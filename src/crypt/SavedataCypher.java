/*  MHTools - MH savedata decrypter/encrypter
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

package crypt;

import static keys.SavedataKeys.mod_a;
import static keys.SavedataKeys.mod_b;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import base.Window;
import keys.SavedataKeys;

public class SavedataCypher extends DecryptUtils implements SavedataKeys {

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

    public void decrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            Window.writeToConsole("Decrypting savedata");
            decrypt_buffer(byte_bt);
            fd.write(byte_bt);
            fd.close();
            Window.writeToConsole("Finished");
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
    
    private void decrypt_buffer(byte buffer[]) {
        int len = buffer.length - 4;
        byte seed[] = new byte[4];
        System.arraycopy(buffer, len, seed, 0, 4);    
        get_table_value(decrypt_table, seed);
        get_table_value(decrypt_table, seed);
        long alpha = get_table_value(seed, 0);
        initSeed(alpha);
        for (int i = 0; i < len; i += 4) {
            set_table_data(buffer, decrypt_table, i);
            alpha = get_table_value(buffer, i);
            long beta = getBeta();
            long gamma = alpha ^ beta;
            set_table_value(buffer, i, gamma);
            set_table_data(buffer, decrypt_table, i);
        }
        System.arraycopy(seed, 0, buffer, len, 4);
    }
    
    public void encrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            System.out.print("Updating ");
            update_sha1(byte_bt);
            Window.writeToConsole("Encrypting savedata");
            encrypt_buffer(byte_bt);
            fd.write(byte_bt);
            fd.close();
            Window.writeToConsole("Finished");
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
    
    private void update_sha1(byte buf[]) {
    	byte replace[] = new byte[20];
    	System.arraycopy(buf, buf.length - 36, replace, 0, 20);
    	System.arraycopy(savedata_sha1_key.getBytes(), 0, buf, buf.length - 36, 20);
        int len = buf.length - 16;
        byte buffer[] = new byte[len];
        System.arraycopy(buf, 0, buffer, 0, len);
        try {
            MessageDigest md = MessageDigest.getInstance("sha-1");
            byte digest[] = md.digest(buffer);
            System.arraycopy(replace, 0, buf, buf.length - 36, 20);
            Window.writeToConsole("SHA-1: " + getHex(digest));
            System.arraycopy(digest, 0, buf, buf.length - 24, digest.length);
        } catch (NoSuchAlgorithmException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
    
    private void encrypt_buffer(byte buffer[]) {
        byte encrypt_table[] = new byte[256];
        for (int i = 0; i < 256; i++) {
            encrypt_table[decrypt_table[i] & 0xFF] = (byte) i;
        }        
        int len = buffer.length - 4;
        byte seed[] = new byte[4];
        System.arraycopy(buffer, len, seed, 0, 4);
        long alpha = get_table_value(seed, 0);
        initSeed(alpha);
        for (int i = 0; i < len; i += 4) {
        	set_table_data(buffer, encrypt_table, i);
            long gamma = get_table_value(buffer, i);
            long beta = getBeta();
            alpha = beta ^ gamma;
            set_table_value(buffer, i, alpha);
            set_table_data(buffer, encrypt_table, i);
        }
        get_table_value(encrypt_table, seed);
        get_table_value(encrypt_table, seed);
        System.arraycopy(seed, 0, buffer, len, 4);
    }
}
