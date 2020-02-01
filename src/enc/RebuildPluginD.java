/*  MHTools - TMH image rebuilder
    Copyright (C) 2011 Codestation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package enc;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.exception.ExceptionUtils;

import base.Encoder;
import base.MHUtils;
import base.Window;
import img.Gim;

/**
 * RebuildPluginD v1.0
 * 
 * @author Codestation
 */
public class RebuildPluginD extends MHUtils implements Encoder {
    
    private byte id[] = {0x2e, 0x54, 0x4d, 0x48, 0x30, 0x2e, 0x31, 0x34};

    @Override
    public void compile(String filepath) {
        try {
            File dir = new File(filepath);
            if(!dir.isDirectory()) {
                Window.writeToConsole("Isn't a directory: " + filepath);
                return;
            }
            File files[] = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            if(files.length > 0) {
                FileOutputStream out = new FileOutputStream(dir.getName() + ".tmh");
                out.write(id);
                writeInt(out, files.length);
                writeInt(out, 0);
                for(File file : files) {
                    Window.writeToConsole("Processing " + file.getName());
                    if(file.isDirectory()) {
                        File subfiles[] = file.listFiles();
                        Arrays.sort(subfiles, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        if(subfiles.length > 0) {
                            Gim gim = new Gim();
                            int counter = 0;
                            Window.writeToConsole("> Processing multi palette image");
                            for(File subfile : subfiles) {
                                Window.writeToConsole("> Processing " + subfile.getName());
                                BufferedImage img = ImageIO.read(subfile);
                                int[] rgbArray = new int[img.getWidth() * img.getHeight()];
                                img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgbArray, 0, img.getWidth());
                                int type;
                                if(file.getName().contains("palette"))
                                    type = Gim.INDEX_8;
                                else if(file.getName().contains("pixels"))
                                    type = Gim.INDEX_4;
                                else
                                    type = 0;
                                int depth;
                                if(file.getName().contains("RGBA8888"))
                                    depth = Gim.RGBA8888;
                                else if(file.getName().contains("RGBA5551"))
                                    depth = Gim.RGBA5551;
                                else
                                    depth = 0;
                                if(!gim.setRGBarray(img.getWidth(), img.getHeight(), rgbArray, type, depth, counter++)) {
                                    Window.writeToConsole("Create RGB array failed");
                                }
                            }                            
                            gim.write(out);
                        }
                        continue;
                    }
                    Gim gim = new Gim();
                    if(file.getName().endsWith(".gim")) {
                        FileInputStream in = new FileInputStream(file);
                        gim.load(in);
                        in.close();
                    } else {
                        BufferedImage img = ImageIO.read(file);
                        int[] rgbArray = new int[img.getWidth() * img.getHeight()];
                        img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgbArray, 0, img.getWidth());
                        int type;
                        if(file.getName().contains("palette"))
                            type = Gim.INDEX_8;
                        else if(file.getName().contains("pixels"))
                            type = Gim.INDEX_4;
                        else
                            type = 0;
                        int depth;
                        if(file.getName().contains("RGBA8888"))
                            depth = Gim.RGBA8888;
                        else if(file.getName().contains("RGBA5551"))
                            depth = Gim.RGBA5551;
                        else
                            depth = 0;
                        if(!gim.setRGBarray(img.getWidth(), img.getHeight(), rgbArray, type, depth, 0)) {
                            Window.writeToConsole("Create RGB array failed");
                        }
                    }
                    gim.write(out);
                }
                out.close();
            } else {
                Window.writeToConsole("Empty directory\n");                
            }               
            Window.writeToConsole("Finished!");
        } catch (FileNotFoundException e) {
        	Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
}
