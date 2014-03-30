package revolve;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortEvent;
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
            assertTrue(serialPort.isOpened());
            serialPort.setParams(
                    SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );//Set params
//            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
//            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new changed());
        } catch (SerialPortException e) {
            e.printStackTrace();
            System.err.println("COM-port Error: There was a problem with opening the port " + serialPort.getPortName());
            fail("Opening port failed!");
        }
    }

    @Test
    public void receiveDataFromTelemetryDevice() throws Exception{
        assertNotNull(serialPort);
        assertTrue(serialPort.writeString("0"));

        for(int i = 0; i < 1000; i++){
            StringBuilder builder = new StringBuilder();
            assertTrue(serialPort.writeString("11111111"));
            System.out.println(serialPort.getOutputBufferBytesCount());
            serialPort.purgePort(SerialPort.PURGE_TXCLEAR);
            System.out.println(serialPort.getOutputBufferBytesCount());
            boolean shouldRun = true;

            long start = Calendar.getInstance().getTimeInMillis();
            while(shouldRun){
                try {
                    builder.append(serialPort.readBytes(1, 1000));
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

    static class changed implements SerialPortEventListener {


        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            System.out.println(serialPortEvent.getEventType());
//            System.out.println((char) serialPortEvent.getEventValue());
        }
    }

}
