package io.opensphere.core.util;

import java.util.concurrent.Phaser;

/**
 * A listener which can provide status as to whether it is prepared to accept a
 * state change. Typically the notifier should request permission from each
 * listener to switch states. Once each listener has become ready to switch,
 * each listener will be given the opportunity to handle any pre-commit detail
 * which are required. Once pre-commit has been completed the new state is
 * committed.
 *
 * @param <T> The type which contains the details of the state change.
 */
public interface ThreePhaseChangeListener<T>
{
    /**
     * Commit the pending state. A listener cannot reject a commit.
     *
     * @param state The state which is being committed.
     * @param phaser A phaser that may be used by other threads to delay the
     *            state transition.
     */
    void commit(T state, Phaser phaser);

    /**
     * Allow listeners to handle an pre-commit details. Any listener which
     * returns true from this method should guarantee that it will be able to
     * commit.
     *
     * @param pendingState The state which will be committed.
     * @param phaser A phaser that may be used by other threads to delay the
     *            state transition.
     * @return true when the listener accepts the pre-commit request.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    boolean preCommit(T pendingState, Phaser phaser) throws PropertyChangeException, InterruptedException;

    /**
     * Request that the listener count down the latch when it is prepared to
     * switch to the pending state.
     *
     * @param pendingState The state which will be committed.
     * @param phaser A phaser that may be used by other threads to delay the
     *            state transition.
     * @return true when the listener accepts the prepare request.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    boolean prepare(T pendingState, Phaser phaser) throws PropertyChangeException, InterruptedException;
}
