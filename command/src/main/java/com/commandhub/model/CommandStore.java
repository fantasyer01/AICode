package com.commandhub.model;

import java.util.ArrayList;
import java.util.List;

public class CommandStore {

    private List<Command> commands = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private int version = 1;

    public CommandStore() {}

    public List<Command> getCommands() { return commands; }
    public void setCommands(List<Command> commands) { this.commands = commands; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
