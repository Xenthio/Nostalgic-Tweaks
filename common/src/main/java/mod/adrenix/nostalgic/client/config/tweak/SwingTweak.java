package mod.adrenix.nostalgic.client.config.tweak;

import mod.adrenix.nostalgic.client.config.reflect.GroupType;

public enum SwingTweak implements ITweak
{
    OVERRIDE_SPEEDS;

    /* Implementation */

    private String key;
    private boolean loaded = false;

    @Override public String getKey() { return this.key; }
    @Override public GroupType getGroup() { return GroupType.SWING; }

    @Override public boolean isLoaded() { return this.loaded; }
    @Override public void setLoaded(boolean state) { this.loaded = state; }
    @Override public void setKey(String key) { this.key = key; }
}
