public class KeyStep extends Step {

    private int keyCode;

    public KeyStep(String type, int keyCode) {
        this.type = type;
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

}
