/**
 * 
 */
package uk.co.recipes.myrrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.Test;

/**
 * For n=100,000 and 100 runs,   mean = 1.868 msecs, median = 1.628 msecs
 * For n=1,000,000 and 100 runs, mean = 75.51 msecs, median = 71.77 msecs
 * 
 * (removing quickest and slowest scores)
 * 
 * @author andrewregan
 *
 */
public class RescorerIdsFileIOTest {

    @Test
    public void testWriting() throws IOException {
//    	for ( int i = 0; i < 100; i++)
    		doTestWriting(100000);
    }

    private void doTestWriting( int inCount) throws IOException {
    	long ns = System.nanoTime();

    	long arr[] = new long[inCount];
    	for ( int i = 0; i < 10000; i++) {
    		arr[i] = i + 1;
    	}
    	long curr = 0x3fffffffffffffffL;
    	for ( int i = 1000; i < arr.length; i++) {
    		arr[i] = curr++;
    	}

        ByteBuffer byteBuffer = ByteBuffer.allocate(arr.length * 8);        
        LongBuffer lb = byteBuffer.asLongBuffer();
        lb.put(arr);

        Path f = Files.createTempFile("foo", "bar");
        File file = f.toFile();
        file.deleteOnExit();
        System.out.println(file);

        FileChannel fc = null;

        try {

            fc = new FileOutputStream(file).getChannel();
            fc.write(byteBuffer);

        } finally {

            if (fc != null)
                fc.close();
        }

        System.out.println( arr.length + " entries written in " + (( System.nanoTime() - ns) / 1000000d) + " msecs");
    }
}
