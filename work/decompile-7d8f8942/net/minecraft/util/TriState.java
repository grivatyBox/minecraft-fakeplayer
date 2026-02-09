package net.minecraft.util;

public enum TriState {

    TRUE, FALSE, DEFAULT;

    private TriState() {}

    public boolean toBoolean(boolean flag) {
        boolean flag1;

        switch (this.ordinal()) {
            case 0:
                flag1 = true;
                break;
            case 1:
                flag1 = false;
                break;
            default:
                flag1 = flag;
        }

        return flag1;
    }
}
