/*  MHTools - MH data.bin/xxxx.bin decrypter
    Copyright (C) 2008-2011 Codestation

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

package crypt;

import static keys.DataKeys.mod_a;
import static keys.DataKeys.mod_b;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.exception.ExceptionUtils;

import base.MHUtils;
import base.Window;
import keys.DataKeys;

public class Decrypter extends DecryptUtils implements DataKeys {
    
    private byte png[] = {(byte) 0x89, 0x50, 0x4e, 0x47};
    private byte gif[] = {0x47, 0x49, 0x46, 0x38};
    private byte tmh[] = {0x2e, 0x54, 0x4d, 0x48};
    private byte gim[] = {0x4d, 0x49, 0x47, 0x2e};
    private byte mwo[] = {0x4d, 0x57, 0x6f, 0x33};
    private byte head[] = {0x48, 0x65, 0x61, 0x64};
    private byte dbst[] = {0x64, 0x62, 0x73, 0x54};
    private byte wav[] = {0x52, 0x49, 0x46, 0x46};
    
    @Override
    protected byte[] getDecryptTable() {
        return decrypter_table;
    }
    
    @Override
    protected long getSeedKeyA() {
        return seed_a;
    }

    @Override
    protected long getSeedKeyB() {
        return seed_b;
    }

    @Override
    protected long getModA() {
        return mod_a;
    }

    @Override
    protected long getModB() {
        return mod_b;
    }

    public void decrypt_index(String in, ByteArrayOutputStream index_buffer) {
        try {
            RandomAccessFile filein = new RandomAccessFile(in, "r");
            RandomAccessFile fileout = new RandomAccessFile("index.bin", "rw");
            fileout.setLength(0);
            byte[] buffer = new byte[4];
            Window.writeToConsole("Decrypting index...");
            initSeed(0);
            boolean table_end = false;
            boolean end_flag = false;
            int i = 0;
            while (!table_end) {
                filein.read(buffer);
                get_table_value(decrypt_table, buffer);
                long beta = getBeta();
                long alpha = get_table_value(buffer, 0);
                long gamma = alpha ^ beta;

                if (gamma > 0xFF) {
                    end_flag = true;
                } else if (end_flag) {
                    table_end = true;
                    continue;
                }
                set_table_value(buffer, 0, gamma);
                fileout.write(buffer);
                if (index_buffer != null)
                    index_buffer.write(buffer);
                i += 4;
            }
            fileout.close();
            Window.writeToConsole("Index size: " + i + " bytes");
            Window.writeToConsole("File count: " + i / 4);
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        }
    }

    public void decrypt_whole(String in, String out) {
        ByteArrayOutputStream index_buffer = new ByteArrayOutputStream();
        decrypt_index(in, index_buffer);
        RandomAccessFile filein;
        try {
            filein = new RandomAccessFile(in, "r");
            byte index_table[] = index_buffer.toByteArray();
            int files_count = index_table.length / 4;
            new File(out).mkdir();
            boolean create_subdirectory = true;
            int last_subdirectory = 0;
            long last_offset = 0;
            for (int i = 0; i < files_count; i++) {
                if (create_subdirectory) {
                    last_subdirectory = i / 1000;
                    new File(out + "/0" + Integer.toString(last_subdirectory))
                            .mkdir();
                    create_subdirectory = false;
                } else {
                    if (last_subdirectory < i / 1000) {
                        create_subdirectory = true;
                    }
                }
                long offset = last_offset;
                last_offset = get_table_value(index_table, i * 4);
                long file_length = (get_table_value(index_table, i * 4) - offset) << 11;
                String fileout = out + "/0"
                        + Integer.toString(last_subdirectory) + "/"
                        + String.format("%04d", i);
                Window.writeToConsole("Decrypting " + fileout + "(" + file_length
                        + " bytes/offset: " + (offset << 11) + ") ... ");
                decrypt_internal(filein, offset, file_length, fileout, false);

            }
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        }
    }
    private boolean equals(byte a1[], byte a2[]) {
        return a1[0] == a2[0] &&
        a1[1] == a2[1] &&
        a1[2] == a2[2] &&
        a1[3] == a2[3];
    }
    
    private String identify_file(byte buffer[], int size) {
        if(size < 4)
            return "bin";
        if(equals(buffer, png))
            return "png";
        if(equals(buffer, gif))
            return "gif";
        if(equals(buffer, tmh))
            return "tmh";
        if(equals(buffer, gim))
            return "gim";
        if(equals(buffer, mwo))
            return "mwo";
        if(equals(buffer, head))
            return "head";
        if(equals(buffer, dbst))
            return "dbst";
        if(equals(buffer, wav))
            return "wav";
        if(size >= 8) {
            if((buffer[0] == 0x3 && buffer[4] == 0x0) ||
               (buffer[0] == 0x3 && buffer[4] == 0x20) ||
               (buffer[0] == 0x4 && buffer[4] == 0x30) ||
               (buffer[0] == 0x6 && buffer[4] == 0x40) ||
               (buffer[0] == 0x7 && buffer[4] == 0x40) ||
               (buffer[0] == 0x8 && buffer[4] == 0x50) ||               
               (buffer[0] == 0xC && buffer[4] == 0x70) ||
               (buffer[0] == 0xD && buffer[4] == 0x70) ||
               (buffer[0] == 0xE && buffer[4] == (byte)0x80) ||
               (buffer[0] == 0xF && buffer[4] == (byte)0x80) ||
               (buffer[0] == 0x10 && buffer[4] == (byte)0x90) ||
               (buffer[0] == 0x28 && buffer[4] == (byte)0x150)) {               
                return "pak";
            }
        }
        return "bin";        
    }

    private void decrypt_internal(RandomAccessFile filein, long pos, long size,
            String out, boolean single) {
        try {
            if (!single)
                filein.seek(pos << 11);
            RandomAccessFile fileout = null;
            byte buffer[] = new byte[1024];
            initSeed(pos);
            boolean create_file = true;
            while (size > 0) {
                int read = filein.read(buffer);
                size -= read;
                for (int i = 0; i < read; i += 4) {
                    set_table_data(buffer, decrypt_table, i);
                    long alpha = get_table_value(buffer, i);
                    long beta = getBeta();
                    long gamma = alpha ^ beta;
                    set_table_value(buffer, i, gamma);
                }
                if(create_file) {
                    String ext = identify_file(buffer, read);
                    fileout = new RandomAccessFile(out + "." + ext, "rw");
                    fileout.setLength(0);
                    create_file = false;
                }
                fileout.write(buffer);
            }
            if(create_file) {
                String ext = identify_file(buffer, 0);
                fileout = new RandomAccessFile(out + "." + ext, "rw");
                fileout.setLength(0);
                create_file = false;
            }
            fileout.close();
            Window.writeToConsole("Finished!");
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        }
    }

    public void decrypt(String in, String out) {
        RandomAccessFile filein = null;
        try {
            filein = new RandomAccessFile(in, "r");
            Window.writeToConsole("Decrypting " + out + " ... ");
            decrypt_internal(filein, MHUtils.getOffset(MHUtils.extractNumber(in)),
                    filein.length(), out, true);

        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
        } finally {
            if (filein != null)
                try {
                    filein.close();
                } catch (IOException e) {
                    Window.writeToConsole(ExceptionUtils.getStackTrace(e));;
                }
        }
    }
}
