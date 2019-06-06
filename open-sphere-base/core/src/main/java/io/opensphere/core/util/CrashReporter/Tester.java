package io.opensphere.core.util.CrashReporter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Tester
{
    /* public void Tester()
    {
        JsonNodeFactory jnf = JsonNodeFactory.instance;
        ObjectNode payload = jnf.objectNode();
        {
            ObjectNode update = root.putObject("update");
            {
            }
            ObjectNode fields = root.putObject("fields");
            {
                fields.put("summary", "something's wrong");
                ObjectNode issuetype = fields.putObject("issuetype");
                {
                    issuetype.put("id", "10000");
                }
                ArrayNode components = fields.putArray("components");
                ObjectNode components0 = components.addObject();
                {
                    components0.put("id", "10000");
                }
                fields.put("customfield_20000", "06/Jul/19 3:25 PM");
                ObjectNode customfield_40000 = fields.putObject("customfield_40000");
                {
                    customfield_40000.put("type", "doc");
                    customfield_40000.put("version", 1);
                    ArrayNode content = customfield_40000.putArray("content");
                    ObjectNode content0 = content.addObject();
                    {
                        content0.put("type", "paragraph");
                        ArrayNode content = content0.putArray("content");
                        ObjectNode content0 = content.addObject();
                        {
                            content0.put("text", "this is a text field");
                            content0.put("type", "text");
                        }
                    }
                }
                ArrayNode customfield_70000 = fields.putArray("customfield_70000");
                customfield_70000.add("jira-administrators");
                customfield_70000.add("jira-software-users");
                ObjectNode project = fields.putObject("project");
                {
                    project.put("id", "10000");
                }
                ObjectNode description = fields.putObject("description");
                {
                    description.put("type", "doc");
                    description.put("version", 1);
                    ArrayNode content = description.putArray("content");
                    ObjectNode content0 = content.addObject();
                    {
                        content0.put("type", "paragraph");
                        ArrayNode content = content0.putArray("content");
                        ObjectNode content0 = content.addObject();
                        {
                            content0.put("text", "description");
                            content0.put("type", "text");
                        }
                    }
                }
                ObjectNode reporter = fields.putObject("reporter");
                {
                    reporter.put("id", "99:e2a16dd9-2ffb-4a4b-a9bd-bd1145a020ee");
                }
                ArrayNode fixVersions = fields.putArray("fixVersions");
                ObjectNode fixVersions0 = fixVersions.addObject();
                {
                    fixVersions0.put("id", "10001");
                }
                fields.put("customfield_10000", "09/Jun/19");
                ObjectNode priority = fields.putObject("priority");
                {
                    priority.put("id", "20000");
                }
                ArrayNode labels = fields.putArray("labels");
                labels.add("bugfix");
                labels.add("blitz_test");
                ObjectNode timetracking = fields.putObject("timetracking");
                {
                    timetracking.put("remainingEstimate", "5");
                    timetracking.put("originalEstimate", "10");
                }
                ArrayNode customfield_30000 = fields.putArray("customfield_30000");
                customfield_30000.add("10000");
                customfield_30000.add("10002");
                ObjectNode customfield_80000 = fields.putObject("customfield_80000");
                {
                    customfield_80000.put("value", "red");
                }
                ObjectNode security = fields.putObject("security");
                {
                    security.put("id", "10000");
                }
                ObjectNode environment = fields.putObject("environment");
                {
                    environment.put("type", "doc");
                    environment.put("version", 1);
                    ArrayNode content = environment.putArray("content");
                    ObjectNode content0 = content.addObject();
                    {
                        content0.put("type", "paragraph");
                        ArrayNode content = content0.putArray("content");
                        ObjectNode content0 = content.addObject();
                        {
                            content0.put("text", "environment");
                            content0.put("type", "text");
                        }
                    }
                }
                ArrayNode versions = fields.putArray("versions");
                ObjectNode versions0 = versions.addObject();
                {
                    versions0.put("id", "10000");
                }
                fields.put("duedate", "2019-05-11T00:00:00.000Z");
                fields.put("customfield_60000", "jira-software-users");
                ObjectNode customfield_50000 = fields.putObject("customfield_50000");
                {
                    customfield_50000.put("type", "doc");
                    customfield_50000.put("version", 1);
                    ArrayNode content = customfield_50000.putArray("content");
                    ObjectNode content0 = content.addObject();
                    {
                        content0.put("type", "paragraph");
                        ArrayNode content = content0.putArray("content");
                        ObjectNode content0 = content.addObject();
                        {
                            content0.put("text", "this is a text area. big text.");
                            content0.put("type", "text");
                        }
                    }
                }
                ObjectNode assignee = fields.putObject("assignee");
                {
                    assignee.put("id", "e5:e1a16c14-1fe0-1c93-a2b1-ac4493ace0f1");
                }
            }
        }

        // Connect Jackson ObjectMapper to Unirest
        Unirest.setObjectMapper(new ObjectMapper()
        {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType)
            {
                try
                {
                    return jacksonObjectMapper.readValue(value, valueType);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value)
            {
                try
                {
                    return jacksonObjectMapper.writeValueAsString(value);
                }
                catch (JsonProcessingException e)
                {
                    throw new RuntimeException(e);
                }

            }
        });

        // This code sample uses the 'Unirest' library:
        // http://unirest.io/java.html
        HttpResponse<JsonNode> response = Unirest.post("/rest/api/3/issue").header("Accept", "application/json")
                .header("Content-Type", "application/json").header("Bearer", "<access_token>").body(payload).asJson();

        System.out.println(response.getBody());

    } */

}
