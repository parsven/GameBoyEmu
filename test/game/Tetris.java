package game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Tetris {
	private static byte[] romContent__ = null;

	static {
		try {
			romContent__ = read("TETRIS.GB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getRomContent() {
		byte[] romContent = new byte[romContent__.length];
		System.arraycopy(romContent__,0,romContent, 0, romContent__.length);
		return romContent;
	}
	private static byte[] read(String fileName) throws IOException {
		File file = new File(fileName);
		FileInputStream fin = new FileInputStream(file);
		int len = (int) file.length();
		byte fileContent[] = new byte[len];
		int readSoFar = 0;
		do {
			readSoFar += fin.read(fileContent, readSoFar, len - readSoFar);

		} while(len - readSoFar > 0);
		return fileContent;
	}
}
