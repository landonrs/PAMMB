public class MouseStep extends Step {

    private int xClick;
    private int yClick;

    public MouseStep(String type, int xCoord, int yCoord) {
        this.type = type;
        this.xClick = xCoord;
        this.yClick = yCoord;
    }

    public int getxClick() {
        return xClick;
    }

    public int getyClick() {
        return yClick;
    }

}
