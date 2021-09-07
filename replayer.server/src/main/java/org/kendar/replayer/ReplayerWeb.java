package org.kendar.replayer;

import org.kendar.http.StaticWebFilter;
import org.kendar.http.annotations.HttpTypeFilter;
import org.kendar.utils.FileResourcesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@HttpTypeFilter(hostAddress = "${replayer.address:replayer.local.org}")
public class ReplayerWeb extends StaticWebFilter {
    @Value("${replayer.path:web}")
    private String path;

    public ReplayerWeb(FileResourcesUtils fileResourcesUtils) {
        super(fileResourcesUtils);
    }

    @Override
    public String getId() {
        return "org.kendar.replayer.ReplayerWeb";
    }

    @Override
    protected String getPath() {
        return path;
    }
}