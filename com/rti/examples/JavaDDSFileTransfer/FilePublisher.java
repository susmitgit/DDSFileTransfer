
package com.rti.examples.JavaDDSFileTransfer;

/* FileFragmentPublisher.java
 * 
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.io.*;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;

import com.rti.examples.util.ProgramOptions;
import com.rti.examples.util.ProgramOptions.*;

// ===========================================================================

public class FilePublisher {

    private static String qos_library = new String ("DDSFileTransferLibrary");
    private static String qos_profile = new String("FileTransferQos");
    private static DomainParticipant participant = null;
    private static Publisher publisher = null;
    private static Topic topic = null;
    private static FileFragmentDataWriter writer = null;
    private static File baseDirectory = null;
    
    /**
     * Map storing known files.
     */
    private static final Map<String, Long> filesFound = new HashMap<String, Long>();
    
    /**
     * The filter which will accept or reject files based on both the
     * <code>includeFilters</code> and the <code>excludeFilters</code> arguments
     * to the constructor.
     */
    private static FilenameFilter filenameFilter;
    
    /**
     * FilePublisher command-line options.
     */
    static final class Opt {
        /**
         * Used to manipulate Options as a group.
         */
        static ProgramOptions All = new ProgramOptions();

        /**
         * Specifies domain ID for participant (0 by default).
         */
        static final Option DOMAIN_ID = Option.makeOption(All, "domainID", int.class, "57");

        /**
         * Specifies number of subscribers to wait for before publishing (1 by
         * default).
         */
        static final Option NUM_SUBSCRIBERS = Option.makeOption(All, "subscribers", int.class, "1");

        /**
         * Root directory from which to publish files.
         */
        static final Option BASE_DIRECTORY = Option.makeStringOption(All, "baseDir", ".");

        /**
         * Determines whether the files in subdirectories will be published or
         * not.
         */
        static final Option RECURSE_SUBDIRECTORIES = Option.makeBooleanOption(All, "norecurse", true);

        /**
         * If true, files modified after transfer will be resent. Note that a
         * new reader will always cause all files to be resent anyway.
         */
        static final Option RESEND_MODIFIED_FILES = Option.makeBooleanOption(All, "noresend", true);

        /**
         * Regular expression specifying files to be included. Overrides exclude
         * filter.
         */
        static final Option INCLUDE_FILTER = Option.makeStringOption(All, "includeFilter", "");

        /**
         * Regular expression specifying files to be excluded.
         */
        static final Option EXCLUDE_FILTER = Option.makeStringOption(All, "excludeFilter", "");

    }
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        int domainId = 0;

        try {
            Opt.All.parseOptions(args);
        } catch (IllegalArgumentException iae) {
            System.err.println("Unknown option: " + iae.getMessage());
            System.err.println("Usage: \n" + Opt.All.getPrintableDescription());
            System.exit(-1);
        }

        
        // Confirm that directory containing files to publish exists
        String baseDir = Opt.BASE_DIRECTORY.asString();
        
        baseDirectory = new File(baseDir).getAbsoluteFile();
        if (!baseDirectory.exists()) {
            System.err.println("Base directory does not exist: " + baseDirectory);
            System.exit(-1);
        }
        
        domainId = Opt.DOMAIN_ID.asInt();
        
        filenameFilter = new JavaDDSFileTransferFilenameFilter(
                Opt.INCLUDE_FILTER.asStringList(","),
                Opt.EXCLUDE_FILTER.asStringList(","));

        // --- Run --- //
        publisherMain(domainId);
    }

    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------

    private FilePublisher() {
        super();
    }

    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId) {

        try {
            participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant_with_profile(
                domainId, qos_library, qos_profile,
                null , StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        

            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   

            String typeName = FileFragmentTypeSupport.get_type_name();
            FileFragmentTypeSupport.register_type(participant, typeName);

            topic = participant.create_topic(
                "FileFragments",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           

            writer = (FileFragmentDataWriter)
            publisher.create_datawriter_with_profile(
                topic, qos_library, qos_profile,
                null, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            // Wait for readers
            PublicationMatchedStatus status = new PublicationMatchedStatus();
            
            System.out.println("Waiting for subscribers...");
            
            while (true) {
                writer.get_publication_matched_status(status);
                
                // check to see if discovered at least #NUM_SUBSCRIBERS
                if (status.current_count >= Opt.NUM_SUBSCRIBERS.asInt()) {
                    System.out.println("Found " + status.current_count + " subscribers...");
                	break;
                }
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    // this is not a problem
                }  
            } 

            /* Main loop

		   	   Get list of files to send
		   	         ? recurse
		   	         ? check to see if files have changed
		   	   Send files one at a time   
		   	*/
                        
            while (true) {

                List<File> fileList = new ArrayList<File>(0);
                        
                GetFilesToSend(fileList, baseDirectory);
                
                System.out.println("Found " + fileList.size() + " files");
                if (fileList.size() == 0) {
            
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        // this is not a problem
                    }  
                    continue;
                }
                
                for (int i=0; i<fileList.size(); ++i) {
                    SendFile(fileList.get(i));
                }
                
            }
        } finally {

            // --- Shutdown --- //
            if (participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.delete_participant(participant);
            }
        }
    }
    
    
    private static void SendFile(File file) {
        
        // ensure that we got a file and that we can read the file before
        // starting 
        if (file == null || !file.exists()) {
            return;
        }
       
        InputStream inputStream = null;        
        
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            
            byte[] inputData = new byte[FILE_FRAGMENT_SIZE_MAX.VALUE];

            int totalFragments = (int) (file.length() / FILE_FRAGMENT_SIZE_MAX.VALUE);
            
            // Check for partial fragment at end of file
            if ((file.length() - (totalFragments*FILE_FRAGMENT_SIZE_MAX.VALUE)) > 0 ) {
                ++totalFragments;
            }
           
            FileFragment fileFrag = new FileFragment();

            // Note, not checking for exceeding FILE_NAME_LENGTH_MAX
            fileFrag.file_name = file.getPath().substring(baseDirectory.getAbsolutePath().length()+1);
            fileFrag.file_size = (int) file.length();
            fileFrag.frag_total = totalFragments;
     
            System.out.println("Sending " + totalFragments + " fragments for file " + fileFrag.file_name);

            int totalBytesRead = 0;
            
            // Send loop
            while (totalBytesRead < file.length()) {
                
                int currentBytesRead = 0;
                
                // reset length of fileFrag to 0
                fileFrag.frag.clear();
                // incr fragment number
                fileFrag.frag_num++;
                
                currentBytesRead = inputStream.read(inputData);

                if (currentBytesRead == inputData.length) {
                    fileFrag.frag.addAllByte(inputData);
                } 
                else if (currentBytesRead > 0) {
                    fileFrag.frag.setSize(currentBytesRead);
                    fileFrag.frag.setByte(0, inputData, 0, currentBytesRead);
                } else {
                    // No more bytes read, should be at end of file
                    continue;
                }
                
                try {
                    // Use DDS to send file fragment
                    writer.write(fileFrag, InstanceHandle_t.HANDLE_NIL);
                }  catch (RETCODE_ERROR err) {
                    System.err.println("Write returned error" + err.toString());
                }  
                
                
                totalBytesRead += currentBytesRead;
            }

        } catch (IOException ioException) {
            // this is sometimes expected; for example, when you are
            // copying a large file the OS might report that the file
            // exists but is unavailable for reading (since it's still
            // being written)

            System.err.println("IOException, failed to send " + file);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    } 
    
    
    private static void GetFilesToSend(List<File> fileList, File dir) {
        
        File[] children = dir.listFiles(filenameFilter);
        
        for (int i = 0; i < children.length; i++) {
            
            if (children[i].isDirectory() && Opt.RECURSE_SUBDIRECTORIES.asBoolean()) {
                
                // Recursively look for other files.
                GetFilesToSend(fileList, children[i]);
                
            } else if (children[i].isFile()) {
                
                File f = children[i].getAbsoluteFile();

                // Got a potential file, have we seen it before?
                if (filesFound.containsKey(f.toString())) {
                    /*
                     * Resend the file if: The file has been modified
                     * and RESEND_MODIFIED_FILES is true
                     */
                    long lastModified = ((Long) filesFound.get(f.toString())).longValue();
                    
                    if (Opt.RESEND_MODIFIED_FILES.asBoolean() && lastModified != f.lastModified()) {
                        filesFound.put(f.toString(), new Long(f.lastModified()));
                        fileList.add(f);
                    }

                } else {
                    // Not found, so add to both found set and send queue.
                    filesFound.put(f.toString(), new Long(f.lastModified()));
                    fileList.add(f);
                }
            }
        }
    }
    
    /**
     * This class provides a filter for filenames. It takes into account the
     * include and exclude patterns. Note that the filter is applied to the
     * filename, not the path + filename.
     * 
     * @author ken
     */
    private static final class JavaDDSFileTransferFilenameFilter implements
            FilenameFilter {

        /**
         * Standard java.util logger
         */
        private static final Logger logger = Logger
                .getLogger(JavaDDSFileTransferFilenameFilter.class.getName());

        /**
         * List of patterns specifying files to be included for transfer.
         */
        private final List<Pattern> includePatterns;
        /**
         * List of patterns specifying files to be excluded for transfer.
         */
        private final List<Pattern> excludePatterns;

        /**
         * Constructs a <code>FileSegmenterFilenameFilter</code> with the
         * specified inclusion and exclusion lists.
         * 
         * @param includeFilter
         *            A list of regular expression <code>String</code>s that
         *            include files.
         * @param excludeFilter
         *            A list of regular expression <code>String</code>s that
         *            exclude files.
         */
        JavaDDSFileTransferFilenameFilter(List<String> includeFilter,
                List<String> excludeFilter) {
            includePatterns = new ArrayList<Pattern>(includeFilter.size());
            for (int i = 0; i < includeFilter.size(); i++) {
                includePatterns.add(Pattern.compile(includeFilter.get(i)));
            }
            excludePatterns = new ArrayList<Pattern>(excludeFilter.size());
            for (int i = 0; i < excludeFilter.size(); i++) {
                excludePatterns.add(Pattern.compile(excludeFilter.get(i)));
            }
        }

        /**
         * Implementation of the <code>FilenameFilter</code> interface. This
         * method will only accept file names that are not excluded and are
         * included (where a zero-length include filter implies to accept any
         * file name).
         */
        public boolean accept(File dir, String name) {
            boolean match = true;

            // first check if it should be excluded
            boolean exclude = false;
            for (int i = 0; i < excludePatterns.size() && !exclude; i++) {
                Pattern p = (Pattern) excludePatterns.get(i);
                exclude = p.matcher(name).matches();
                logger.finer("exclude " + name + "? : " + exclude);
            }

            if (exclude) {
                // if the file was explicitly excluded, then it is not a match
                match = false;
            } else {
                // if there are no include patterns, accept anything
                match = (includePatterns.size() < 1);
                for (int i = 0; i < includePatterns.size() && !match; i++) {
                    Pattern p = (Pattern) includePatterns.get(i);
                    match = p.matcher(name).matches();
                    logger.finer("match " + name + "? : " + match);
                }
            }
            logger.finer(name + " " + (match ? "matches" : "doesn't match"));
            return match;
        }
    }
}
