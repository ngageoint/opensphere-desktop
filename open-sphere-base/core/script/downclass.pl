#!/usr/bin/perl -nlw
#
# Usage: downclass.pl <bad tokens file> <exceptions file> <num threads>
#
# This searches a null-separated list of files on the standard input for
# the tokens in the newline-separated bad tokens file. Words from the
# newline-separated exceptions file are removed from the input files
# prior to matching the bad tokens.
#
# If any bad tokens are found, they are printed on the standard output along
# with the first file each one occurred in.
#
# The exit code is 1 if any bad tokens are found; 0 otherwise.

use strict;
use threads;
use Thread::Queue;

my ($debug, @exceptions, @badtokens, @threads);
our %map : shared;

sub debug($)
{
  print STDERR shift if $debug;
}

sub processFile
{
  while (my $filename = $::queue->dequeue)
  {
    if ( -f $filename )
    {
      debug "Searching file $filename on thread ".threads->self->tid();
      open(FILE, $filename);
      while (<FILE>)
      {
        my $line = $_;
        for (@exceptions)
        {
          $line =~ s/\Q$_\E/ /ig;
        }
        for (@badtokens)
        {
          if ($line =~ /([-\w]*\Q$_\E[-\w]*)/i)
          {
            my $word = $1;
            $word =~ tr/[A-Z]/[a-z]/;
            my %finding : shared = ( FILENAME => $filename, TOKEN => $_ );
	    lock %map;
            $map{$word} = \%finding;
          }
        }
      }
    }
  }
}

BEGIN
{
  $debug = 1;

  die "Usage: $0 <bad tokens file> <exceptions file> <num threads>" if $#ARGV != 2;

  $/ = "|";
  my $badtokensfile = shift;
  open(FILE, $badtokensfile) or die "Cannot open \"$badtokensfile\".";
  debug "Using tokens file \"$badtokensfile\".";
  chomp(@badtokens = <FILE>);
  debug "Searching for ".scalar(@badtokens)." bad words.";

  $/ = "\n";
  my $exceptionsfile = shift;
  if ( -f $exceptionsfile )
  {
    open(FILE, $exceptionsfile) or die "Cannot open \"$exceptionsfile\".";
    debug "Using exceptions file \"$exceptionsfile\".";
    chomp(@exceptions = <FILE>);
    
    for my $exception (@exceptions)
    {
       for my $token (@badtokens)
       {
          die "Bad token [$token] illegally contains exception [$exception]" if $token =~ /\Q$exception\E/i;
       }
    }
  }
  else
  {
    @exceptions = ();
  }
  debug "Eliminating ".scalar(@exceptions)." exceptions.";
  $/ = "\0";

  $::queue = new Thread::Queue;

  my $numThreads = shift;
  for (1..$numThreads)
  {
    push @threads, new threads(\&processFile);
  }
}

$::queue->enqueue($_);

END
{
  for (@threads)
  {
    $::queue->enqueue(undef);
  }
  for (@threads)
  {
    $_->join;
  }

  lock %map;
  for my $key1 (sort keys(%map))
  {
    my $found = 0;
    for my $key2 (keys(%map))
    {
      if ($key1 ne $key2 && $key1 =~ /\Q$key2\E/)
      {
        $found = 1;
        last
      }
    }
    print "\"$key1\" matches \"$map{$key1}->{TOKEN}\" in file \"$map{$key1}->{FILENAME}\"" unless $found
  }

  my $result = scalar keys %map > 0;
  exit $result;
} 
