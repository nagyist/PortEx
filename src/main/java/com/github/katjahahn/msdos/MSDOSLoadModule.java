/*******************************************************************************
 * Copyright 2014 Katja Hahn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.github.katjahahn.msdos;

import static com.github.katjahahn.msdos.MSDOSHeaderKey.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.github.katjahahn.HeaderKey;
import com.github.katjahahn.PEModule;

/**
 * Responsible for dumping the MSDOS load module.
 * 
 * @author Katja Hahn
 * 
 */
public class MSDOSLoadModule extends PEModule {

	private static final int PAGE_SIZE = 512; // in Byte

	private final MSDOSHeader header;
	private final File file;
	private byte[] loadModuleBytes;

	/**
	 * @constructor creates the MSDOSLoadModule instance based on the
	 *              {@link MSDOSHeader} of the given file
	 * @param header
	 * @param file
	 */
	public MSDOSLoadModule(MSDOSHeader header, File file) {
		this.header = header;
		this.file = file;
	}

	@Override
	public void read() throws IOException {
		long headerSize = header.getHeaderSize();
		int loadModuleSize = getLoadModuleSize();

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			raf.seek(headerSize);
			loadModuleBytes = new byte[loadModuleSize];
			raf.readFully(loadModuleBytes);
		}
	}

	/**
	 * Calculates the size of the load module.
	 * 
	 * @return load module size
	 */
	public int getLoadModuleSize() {
		return (int) (getImageSize() - header.getHeaderSize());
	}

	/**
	 * Calculates the size of the image based on {@link MSDOSHeader} information
	 * FIXME too large, surpasses PE signature
	 * @return image size
	 */
	public int getImageSize() {
		int filePages = header.get(FILE_PAGES).intValue();
		int lastPageSize = header.get(LAST_PAGE_SIZE).intValue();

		int imageSize = (filePages - 1) * PAGE_SIZE + lastPageSize;
		if (lastPageSize == 0) {
			imageSize += PAGE_SIZE;
		}
		return imageSize;
	}

	/**
	 * Returns the bytes of the load module.
	 * 
	 * @return bytes of the load module
	 * @throws IOException
	 */
	public byte[] getDump() throws IOException {
		if (loadModuleBytes == null) {
			read();
		}
		return loadModuleBytes.clone();
	}

	@Override
	public String getInfo() {
		return "----------------" + NL + "MSDOS Load Module" + NL
				+ "----------------" + NL + NL + "image size:" + getImageSize()
				+ NL + "load module size: " + getLoadModuleSize() + NL;
	}

	@Override
	public Long get(HeaderKey key) {
		return null;
	}

	@Override
	public long getOffset() {
		return header.getHeaderSize();
	}

}
