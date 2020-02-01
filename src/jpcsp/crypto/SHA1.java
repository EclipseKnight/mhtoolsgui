/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */

package jpcsp.crypto;

import java.security.MessageDigest;

import org.apache.commons.lang3.exception.ExceptionUtils;

import base.Window;

public class SHA1 {

    public SHA1() {
    }

    public byte[] doSHA1(byte[] bytes, int lenght) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] sha1Hash = new byte[40];
            md.update(bytes, 0, lenght);
            sha1Hash = md.digest();
            return sha1Hash;
        } catch (Exception e) {
            Window.writeToConsole(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}