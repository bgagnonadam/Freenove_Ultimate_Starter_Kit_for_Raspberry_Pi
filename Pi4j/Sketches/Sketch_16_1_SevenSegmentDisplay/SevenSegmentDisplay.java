///usr/bin/env jbang "$0" "$@" ; exit $?  
  
//DEPS org.slf4j:slf4j-api:2.0.12  
//DEPS org.slf4j:slf4j-simple:2.0.12  
//DEPS com.pi4j:pi4j-core:2.6.0  
//DEPS com.pi4j:pi4j-plugin-raspberrypi:2.6.0  
//DEPS com.pi4j:pi4j-plugin-gpiod:2.6.0  
  
import com.pi4j.Pi4J;  
import com.pi4j.context.Context;  
import com.pi4j.io.gpio.digital.DigitalOutput;  
  
class HC595 {  
    public enum Order {LSBFIRST, MSBFIRST};  
  
    private final DigitalOutput dataPin;  
    private final DigitalOutput latchPin;  
    private final DigitalOutput clockPin;  
    private final Context pi4j;  
  
    public HC595(Context pi4j, int dataPin, int latchPin, int clockPin) {  
        this.pi4j = pi4j;  
        this.dataPin = pi4j.dout().create(dataPin);  
        this.latchPin = pi4j.dout().create(latchPin);  
        this.clockPin = pi4j.dout().create(clockPin);  
    }  

    private static void delayUs(long us) {  
        long startTime = System.nanoTime();  
        long endTime = startTime + (us * 1000);  
        while (System.nanoTime() < endTime) {  
        }  
    }  

    public void shiftOut(Order order, int val) {  
        int i;  
        for (i = 0; i < 8; i++) {  
            clockPin.low();  
            if (order == Order.LSBFIRST) {  
                if ((0x01 & (val >> i)) == 0x01) {  
                    dataPin.high();  
                } else {  
                    dataPin.low();  
                }  
            } else {  
                if ((0x80 & (val << i)) == 0x80) {  
                    dataPin.high();  
                } else {  
                    dataPin.low();  
                }  
            }  
            clockPin.high();  
        }  
    }  
  
    public void updateLatch() {  
        latchPin.low();  
        delayUs(1);  
        latchPin.high();  
    }  

    public void shutdown() {  
        pi4j.shutdown();  
    }  
}  
  
public class SevenSegmentDisplay {  
    private static final int DATA_PIN = 22;  
    private static final int LATCH_PIN = 27;  
    private static final int CLOCK_PIN = 17;  
  
    public static void main(String[] args) throws Exception {  
        var pi4j = Pi4J.newAutoContext();  
        HC595 segment = new HC595(pi4j, DATA_PIN, LATCH_PIN, CLOCK_PIN);  
  
        try {  
            int[] nums = {0xffc0, 0xfff9, 0xffa4, 0xffb0, 0xff99, 0xff92, 0xff82, 0xfff8, 0xff80, 0xff90, 0xff88, 0xff83, 0xffc6, 0xffa1, 0xff86, 0xff8e};  
  
            while (true) {  
                for (int num : nums) {  
                    // Shift out the high byte (dummy in this case)  
                    segment.shiftOut(HC595.Order.MSBFIRST, (num >> 8) & 0xff);  
                    // Shift out the low byte containing the actual digit  
                    segment.shiftOut(HC595.Order.MSBFIRST, num & 0xff);  
                    segment.updateLatch();  
                    Thread.sleep(500); // Delay before changing the next digit  
                }  
            }  
        } finally {  
            segment.shutdown();  
        }  
    }  
}
