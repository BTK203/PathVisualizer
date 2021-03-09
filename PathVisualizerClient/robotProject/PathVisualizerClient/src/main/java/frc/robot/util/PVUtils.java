// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;

import edu.wpi.first.wpilibj.DriverStation;

/** 
 * Utilities used by the PVHost. They don't quite fit the functionality of any of the other classes, so they end up here.
 */
public class PVUtils {
    public static String[] getFilesInDirectory(String directory, boolean specifyDirectories) {
        try {
            ArrayList<String> paths = new ArrayList<String>();
            DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(java.nio.file.Path.of(directory));
            Iterator<java.nio.file.Path> iterator = stream.iterator();
            while(iterator.hasNext()) {
                paths.add(iterator.next().toAbsolutePath().toString());
            }
            stream.close();

            //put stuff in string array
            String[] contents = new String[paths.size()];
            for(int i=0; i<paths.size(); i++) {
                String path = paths.get(i);
                if(specifyDirectories) {
                    if(Files.isDirectory(java.nio.file.Path.of(path))) {
                        path += ":dir";
                    } else {
                        path += ":file";
                    }
                }
                contents[i] = path;
            }

            return contents;
        } catch(IOException ex) {
            DriverStation.reportWarning("Tried to look in directory \"" + directory + "\" but encountered problems.", true);
            return new String[0];
        }
    }

    public static String[] getFilesInDirectory(String directory) {
        return getFilesInDirectory(directory, false);
    }
}
