/*
 * Copyright 2017 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.codecs;

import org.bson.BsonBinarySubType;
import org.bson.BsonSerializationException;

import java.util.UUID;

public class UuidHelper {
    public static UUID decode(byte[] bytes, byte subType) {
        if (bytes.length != 16) {
            throw new BsonSerializationException(String.format("Expected length to be 16, not %d.", bytes.length));
        }

        if (subType == BsonBinarySubType.UUID_LEGACY.getValue()) {
            reverseByteArray(bytes, 0, 8);
            reverseByteArray(bytes, 8, 8);
        }

        return new UUID(readLongFromArrayBigEndian(bytes, 0), readLongFromArrayBigEndian(bytes, 8));
    }

    private static void reverseByteArray(final byte[] data, final int start, final int length) {
        for (int left = start, right = start + length - 1; left < right; left++, right--) {
            // swap the values at the left and right indices
            byte temp = data[left];
            data[left]  = data[right];
            data[right] = temp;
        }
    }

    private static long readLongFromArrayBigEndian(final byte[] bytes, final int offset) {
        long x = 0;
        x |= (0xFFL & bytes[offset + 7]);
        x |= (0xFFL & bytes[offset + 6]) << 8;
        x |= (0xFFL & bytes[offset + 5]) << 16;
        x |= (0xFFL & bytes[offset + 4]) << 24;
        x |= (0xFFL & bytes[offset + 3]) << 32;
        x |= (0xFFL & bytes[offset + 2]) << 40;
        x |= (0xFFL & bytes[offset + 1]) << 48;
        x |= (0xFFL & bytes[offset]) << 56;
        return x;
    }
}
