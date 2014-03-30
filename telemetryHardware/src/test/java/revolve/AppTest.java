package revolve;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;



/**
 * Unit test for simple App.
 */
public class AppTest{

    private SerialPort serialPort = null;

    @Before
    public void setUp() throws Exception{
        for(String s : SerialPortList.getPortNames()){
            System.out.println("Serial port:" + s);
        }


        String name = null;
        String[] serialPorts = SerialPortList.getPortNames();
        assertTrue(serialPorts.length > 0);

        if(serialPorts.length == 1){
            name = serialPorts[0];
        }else{
            for(String s : serialPorts){
                if(s.contains("usbmodem12...E1")){
                    name = s;
                    break;
                }
            }
        }
        assertNotNull(name, "Telemetry device not found");

        serialPort = new SerialPort(name);
        try{
            serialPort.openPort();//Open serial port
            serialPort.setParams(115200, 8, 1, 0);//Set params
        } catch (SerialPortException e) {
            e.printStackTrace();
            System.err.println("COM-port Error: There was a problem with opening the port " + serialPort.getPortName());
            fail("Opening port failed!");
        }
    }

    @Test
    public void receiveDataFromTelemetryDevice() throws Exception{
        assertNotNull(serialPort);

        for(int i = 0; i < 1000; i++){
            StringBuilder builder = new StringBuilder();
            serialPort.writeInt(48);
            serialPort.writeInt(49);
            boolean shouldRun = true;

            long start = Calendar.getInstance().getTimeInMillis();
            while(shouldRun){
                try {
                    builder.append(serialPort.readBytes(50, 1000));
                }catch (Exception e){
                    System.out.println("\n" + e);
                    fail("Failed to receive data at run " + i);
                }
                if(builder.toString().contains("\n")){
                    shouldRun = false;
                }
            }
            long stop = Calendar.getInstance().getTimeInMillis();
            assertTrue("Took more than 100ms to receive one line of data", (stop-start < 100));
            System.out.println(builder.toString());
        }
    }

    @After
    public void destroy() throws Exception{
        assertTrue(serialPort.closePort());
        serialPort = null;
    }

}
