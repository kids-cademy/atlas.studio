package com.kidscademy.atlas.studio.tool;

import java.io.File;

import js.tiny.container.annotation.ContextParam;

public class GitProcess extends CommandProcess {
    @ContextParam("git.client")
    private static String COMMAND_PATH;

    public GitProcess(File appDir) {
	super(appDir);
    }

    @Override
    protected String getCommandPath() {
	return COMMAND_PATH;
    }
}
