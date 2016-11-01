package ssl.bluetruth.beans;

public class Result {
    private boolean success;
    private String message;
    private Object data;
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
