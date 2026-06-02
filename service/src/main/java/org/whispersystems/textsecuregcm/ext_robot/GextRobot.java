package org.whispersystems.textsecuregcm.ext_robot;

public class GextRobot {
    private boolean robot = false;
    private GextRobotMsgButtonVisible msgButtonVisible;

    public boolean isRobot() {
        return robot;
    }

    public void setRobot(boolean robot) {
        this.robot = robot;
    }

    public GextRobotMsgButtonVisible getMsgButtonVisible() {
        return msgButtonVisible;
    }

    public void setMsgButtonVisible(GextRobotMsgButtonVisible msgButtonVisible) {
        this.msgButtonVisible = msgButtonVisible;
    }
}
