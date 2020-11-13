import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Controller {
    private String IP;
    private String ID;
    private String MODEL;
    private String NAME;

    private final int PORT = 5577;
    private Map<String, Byte> LastColor;
    private Byte LastWW;
    private Byte LastCW;
    private boolean ColdWhiteSupport = false;
    private static final Map<String, Integer> PATTERNS = new HashMap<>(){{
        put("seven_color_cross_fade", 0x25);
        put("red_gradual_change", 0x26);
        put("green_gradual_change", 0x27);
        put("blue_gradual_change", 0x28);
        put("yellow_gradual_change", 0x29);
        put("cyan_gradual_change", 0x2a);
        put("purple_gradual_change", 0x2b);
        put("white_gradual_change", 0x2c);
        put("red_green_cross_fade", 0x2d);
        put("red_blue_cross_fade", 0x2e);
        put("green_blue_cross_fade", 0x2f);
        put("seven_color_strobe_flash", 0x30);
        put("red_strobe_flash", 0x31);
        put("green_strobe_flash", 0x32);
        put("blue_stobe_flash", 0x33);
        put("yellow_strobe_flash", 0x34);
        put("cyan_strobe_flash", 0x35);
        put("purple_strobe_flash", 0x36);
        put("white_strobe_flash", 0x37);
        put("seven_color_jumping", 0x38);
    }};

    public Controller(String ip, String id, String model) {
        this.IP = ip;
        this.ID = id;
        this.MODEL = model;
        this.NAME = model;

        HashMap<String, Object> state = this.queryState();

        this.LastColor = new HashMap<>() {{
            put("red", (Byte) state.get("colors.red"));
            put("green", (Byte) state.get("colors.green"));
            put("blue", (Byte) state.get("colors.blue"));
        }};
        this.LastWW = (Byte) state.get("ward_white");
        this.LastCW = (Byte) state.get("cold_white");

        if((byte) state.get("type") == 0x35) this.ColdWhiteSupport = true;

    }

    public String getIP() {
        return IP;
    }

    public String getID() {
        return ID;
    }

    public String getMODEL() {
        return MODEL;
    }

    public String getNAME() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Controller cont = (Controller) o;
        return IP.equals(cont.IP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IP);
    }

    public HashMap<String, Object> queryState() {
        byte[] data = this.sendCommand(new byte[]{(byte) 0x81, (byte) 0x8a, (byte) 0x8b});
        if (data == null) return null;
        if(data[13] == 0x0) return null;

        HashMap<String, Object> state = new HashMap<>(){{
            put("type", data[1]);
            put("on", data[2] == 0x23);
            put("mode", determineMode(data));
            put("pattern", determinePattern(data));
            put("speed", determineMode(data) != "ia_pattern" ? delayToSpeed(data[5]) : data[5]);
            put("colors.red", data[6]);
            put("colors.green", data[7]);
            put("colors.blue", data[8]);
            put("warm_white", data[9]);
            put("cold_white", data[11]);
        }};

        this.LastColor = new HashMap<>() {{
            put("red", (Byte)state.get("colors.red"));
            put("green", (Byte)state.get("colors.green"));
            put("blue", (Byte)state.get("colors.blue"));
        }};
        this.LastWW = (Byte)state.get("ward_white");
        this.LastCW = (Byte)state.get("cold_white");

        if((byte) state.get("type") == 0x35) this.ColdWhiteSupport = true;

        return state;
    }

    public boolean setPower(boolean on) {
        byte onByte = 0x24;
        if(on) onByte = 0x23;

        byte[] data = this.sendCommand(new byte[]{0x71, onByte, 0x0f});

        if(data == null) return false;
        if(data[2] == 0x0) return false;
        if(data[2] == onByte) return false;

        return true;
    }

    public boolean setColor(int red, int green, int blue) {
        return this.sendColorCommand(red, green, blue, 0, 0);
    }

    public boolean setWarmWhite(int ww) {
        return this.sendColorCommand(0, 0, 0, ww, 0);
    }

    public boolean setColdWhite(int cw) {
        if(this.ColdWhiteSupport) return this.sendColorCommand(0, 0, 0, cw, 0);
        return false;
    }

    public boolean validateColors() {
        byte red = this.LastColor.get("red");
        byte green = this.LastColor.get("green");
        byte blue = this.LastColor.get("blue");
        byte ww = this.LastWW;
        byte cw = this.LastCW;

        HashMap<String, Object> query = this.queryState();

        boolean redSet = ((byte)query.get("colors.red")) == red;
        boolean greenSet = ((byte)query.get("colors.green")) == green;
        boolean blueSet = ((byte)query.get("colors.blue")) == blue;
        boolean wwSet = ((byte)query.get("warm_white")) == ww;
        if(this.ColdWhiteSupport){
            boolean cwSet = ((byte)query.get("cold_white")) == cw;
            return redSet && greenSet && blueSet && wwSet && cwSet;
        }else{
            return redSet && greenSet && blueSet && wwSet;
        }
    }

    private String determineMode(byte[] resp) {
        if(resp[3] == 0x61 || (resp[3] == 0 && resp[4] == 0x61)) {
            return "color";
        }else if(resp[3] == 0x62) {
            return "special";
        }else if(resp[3] == 0x60) {
            return "custom";
        }else if(resp[3] >= 0x25 && resp[3] <= 0x38) {
            return "pattern";
        }else if(resp[3] >= 0x64 && resp[3] <= 0x018f) {
            return "ia_pattern";
        }else {
            return null;
        }
    }

    private String determinePattern(byte[] resp) {
        if(resp[3] >= 0x25 && resp[3] <= 0x38) {
            for (HashMap.Entry<String, Integer> pattern : PATTERNS.entrySet()){
                if(pattern.getValue() == resp[3]) return pattern.getKey();
            }
        }

        return null;
    }

    private int delayToSpeed(int delay){
        delay = clamp(delay, 1, 31);
        delay -= 1;
        return 100 - (delay / 30  * 100);
    }

    private int speedToDelay(int speed){
        speed = clamp(speed, 0, 100);
        return (30 - ((speed / 100) * 30)) + 1;
    }

    private int clamp(int value, int min, int max){
        return value > max ? max : Math.max(value, min);
    }

    private byte [] sendCommand(byte[] buffer) {
        try{
            Socket socket = new Socket(this.IP, 5577);
            socket.setSoTimeout(500);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

            byte[] command = new byte[buffer.length+1];
            int checksum = 0;

            for (int i=0;i<buffer.length;i++){
                checksum += buffer[i];
                command[i] = buffer[i];
            }
            command[command.length-1] = (byte)checksum;

            dOut.write(command);

            DataInputStream dIn = new DataInputStream(socket.getInputStream());

            byte[] resp = new byte[64];

            dIn.read(resp);

            socket.close();
            return resp;
        }catch (Exception e){
            return null;
        }
    }

    private boolean sendColorCommand(int red, int green, int blue, int ww, int cw) {
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);
        ww = clamp(ww, 0, 255);

        byte[] command;
        if(this.ColdWhiteSupport){
            cw = clamp(cw, 0, 255);
            command = new byte[]{0x31, (byte) red, (byte) green, (byte) blue, (byte) ww, (byte) cw, (byte) 0x00, 0x0F, 0x00};
        }else{
            command = new byte[]{0x31, (byte) red, (byte) green, (byte) blue, (byte) ww, (byte) 0x00, 0x0F, 0x00};
        }

        int checksum = 0;
        for (byte b : command) {
            checksum += b;
        }
        command[command.length-1] = (byte)checksum;

        try{
            Socket socket = new Socket(this.IP, 5577);
            socket.setSoTimeout(500);

            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            dOut.write(command);

            int finalRed = red;
            int finalGreen = green;
            int finalBlue = blue;
            this.LastColor = new HashMap<>() {{
                put("red", (byte) finalRed);
                put("green", (byte) finalGreen);
                put("blue", (byte) finalBlue);
            }};
            this.LastWW = (byte) ww;
            this.LastCW = (byte) cw;

            socket.close();
            return true;
        }catch (Exception e){
            return false;
        }
    }

}