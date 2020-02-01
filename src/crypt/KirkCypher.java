/*  MHTools - KIRK savedata decrypter/encrypter
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.exception.ExceptionUtils;

import base.MHUtils;
import base.Window;
import jpcsp.crypto.CryptoEngine;
import keys.GameKeys;

public class KirkCypher extends MHUtils implements GameKeys {
	
    public void decrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            Window.writeToConsole("Decrypting savedata (KIRK engine): " + byte_bt.length + " bytes");
            Window.writeToConsole("Gamekey: " + getHex(gamekey));
            byte out[] = new CryptoEngine().DecryptSavedata(byte_bt, byte_bt.length, gamekey, 0);
            fd.write(out);
			fd.setLength(out.length);
            fd.close();
            Window.writeToConsole("Finished (" + out.length + " bytes)");
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
    
    public void encrypt(String file) {
        try {
            RandomAccessFile fd = new RandomAccessFile(file, "rw");
            byte byte_bt[] = new byte[(int)fd.length()];
            fd.read(byte_bt);
            fd.seek(0);
            Window.writeToConsole("Encrypting savedata (KIRK engine): " + byte_bt.length + " bytes");
            Window.writeToConsole("Gamekey: " + getHex(gamekey));
            CryptoEngine ce = new CryptoEngine();
            byte out[] = ce.EncryptSavedata(byte_bt, byte_bt.length, gamekey, 0);
            fd.write(out);
			fd.setLength(out.length);
            fd.close();
            Window.writeToConsole("Finished (" + out.length + " bytes)");
            byte hash[] = ce.UpdateSavedataHashes(out, out.length, 0);
            RandomAccessFile hashfd = new RandomAccessFile("hash.bin", "rw");
            hashfd.write(hash);
            hashfd.close();
            Window.writeToConsole("Hash saved to hash.bin");
        } catch (FileNotFoundException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        } catch (IOException e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
        }
    }
}
