package model;

import java.util.HashMap;
import java.util.Map;

public class MetaData {

    private Map<String, String> metadataParams;

    public void buildMetadata(String rawMetadata) {
        Map<String, String> metadataParams = new HashMap<>();
        String[] keyValueParamsArray = rawMetadata.split(",");
        for (String rawKeyValue : keyValueParamsArray) {
            String[] keyValueArr = rawKeyValue.split("=");
            if (keyValueArr.length > 1) {
                metadataParams.put(keyValueArr[0], keyValueArr[1]);
            }
        }
        if (!metadataParams.isEmpty()) {
            this.metadataParams = metadataParams;
        }
    }

    public Map<String, String> getMetadataParams() {
        return metadataParams;
    }

    public boolean isMetadataLoaded() {
        return metadataParams != null;
    }

}

