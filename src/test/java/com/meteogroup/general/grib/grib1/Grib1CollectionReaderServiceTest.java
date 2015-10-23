package com.meteogroup.general.grib.grib1;

import com.meteogroup.general.grib.exception.BinaryNumberConversionException;
import com.meteogroup.general.grib.exception.GribReaderException;
import com.meteogroup.general.grib.grib1.model.Grib1Record;
import com.meteogroup.general.grib.util.FileChannelPartReader;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by roijen on 20-Oct-15.
 */
public class Grib1CollectionReaderServiceTest {

    private Grib1CollectionReaderService collectionReader;


    @DataProvider(name = "goodPDSArray")
    public static Object[][] simpleFileLocation(){
        return new Object[][]{
                new Object[]{VERY_SIMPLE_TEXT_FILE_LOCATION}
        };
    }

    @DataProvider(name = "notExistingFileLocation")
    public static Object[][] notExistingFileLocation(){
        return new Object[][]{
                new Object[]{NOT_EXISTING_FILE_LOCATION},
        };
    }


    @BeforeMethod
    public void setUp(){
        collectionReader = new Grib1CollectionReaderService();
    }

    @Test(dataProvider = "simpleFileLocation")
    public void getAFileChannelFromAFileName(String fileLocation) throws IOException {
        String fileName = getClass().getResource(fileLocation).toString().trim().replace("file:/","");
        FileChannel channel = collectionReader.getFileChannelFromURL(fileName);
        assertThat(channel).isNotNull();
        assertThat(collectionReader.getGribRecordOffset()).isEqualTo(0l);
        assertThat(collectionReader.getFileLength()).isGreaterThan(0);
    }

    @Test(dataProvider = "notExistingFileLocation", expectedExceptions = FileNotFoundException.class)
    public void getAFileChannelFromANotExistingFileName(String fileLocation) throws IOException {
        collectionReader.getFileChannelFromURL(fileLocation);
    }

    @Test
    public void testReadRecords() throws GribReaderException, IOException, BinaryNumberConversionException {

        collectionReader.partReader = mock(FileChannelPartReader.class);
        collectionReader.recordReader = mock(Grib1RecordReaderService.class);
        collectionReader.fileLength = 16;
        collectionReader.gribRecordOffset = 0;

        when(collectionReader.partReader.readPartOfFileChannel(any(FileChannel.class), anyInt(), anyInt())).thenReturn(SIMULATED_BYTE_ARRAY);

        when(collectionReader.recordReader.checkIfGribFileIsValidGrib1(any(byte[].class))).thenReturn(true);
        when(collectionReader.recordReader.readRecordLength(any(byte[].class))).thenReturn(8);

        ArrayList<Grib1Record> records = collectionReader.readAllRecords(SIMULATED_FILE_CHANNEL());
        assertThat(records.size()).isEqualTo(2);
    }

    private static final String VERY_SIMPLE_TEXT_FILE_LOCATION = "/grib1test/samplefiles/VerySimpleSampleFile.txt";

    private static final String NOT_EXISTING_FILE_LOCATION = "/dev/null/doesnotexist.txt";

    private static final byte[] SIMULATED_BYTE_ARRAY = new byte[]{'G','R','I','B',19,84,-26,1};
    private static FileChannel SIMULATED_FILE_CHANNEL() throws FileNotFoundException {
        String fileName = Grib1CollectionReaderServiceTest.class.getClass().getResource("/grib1test/samplefiles/VerySimpleSampleFile.txt").toString().trim().replace("file:/","");
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        return raf.getChannel();
    }

}