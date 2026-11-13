
package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Public Group Topology")
@JsonPropertyOrder({"date", "group", "type", "subgroup", "tags"})
public class PublicGroupTopologyDto {

    @JsonProperty("date")
    public String date;

    @JsonProperty("group")
    public String group;

    @JsonProperty("type")
    public String type;

    @JsonProperty("subgroup")
    public String subgroup;

    @JsonProperty("tags")
    public Map<String, String> tags;
}
