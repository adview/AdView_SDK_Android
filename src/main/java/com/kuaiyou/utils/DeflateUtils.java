package com.kuaiyou.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/***
 * A collection of utility methods for working on deflated data.
 */
public class DeflateUtils {

	// private static final Log LOG = LogFactory.getLog(DeflateUtils.class);
	private static final int EXPECTED_COMPRESSION_RATIO = 5;
	private static final int BUF_SIZE = 4096;

	public static void logInfo(String str) {

	}

	/***
	 * Returns an inflated copy of the input array. If the deflated input has
	 * been truncated or corrupted, a best-effort attempt is made to inflate as
	 * much as possible. If no data can be extracted <code>null</code> is
	 * returned.
	 */
	public static final byte[] inflateBestEffort(byte[] in) {
		return inflateBestEffort(in, Integer.MAX_VALUE);
	}

	/***
	 * Returns an inflated copy of the input array, truncated to
	 * <code>sizeLimit</code> bytes, if necessary. If the deflated input has
	 * been truncated or corrupted, a best-effort attempt is made to inflate as
	 * much as possible. If no data can be extracted <code>null</code> is
	 * returned.
	 */
	public static final byte[] inflateBestEffort(byte[] in, int sizeLimit) {
		// decompress using InflaterInputStream
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(
				EXPECTED_COMPRESSION_RATIO * in.length);

		// "true" because HTTP does not provide zlib headers
		Inflater inflater = new Inflater(true);
		InflaterInputStream inStream = new InflaterInputStream(
				new ByteArrayInputStream(in), inflater);

		byte[] buf = new byte[BUF_SIZE];
		int written = 0;
		while (true) {
			try {
				int size = inStream.read(buf);
				if (size <= 0)
					break;
				if ((written + size) > sizeLimit) {
					outStream.write(buf, 0, sizeLimit - written);
					break;
				}
				outStream.write(buf, 0, size);
				written += size;
			} catch (Exception e) {
				logInfo("Caught Exception in inflateBestEffort");
				e.printStackTrace();
				break;
			}
		}
		try {
			outStream.close();
		} catch (IOException e) {
		}

		return outStream.toByteArray();
	}


	/***
	 * Returns an inflated copy of the input array.
	 * 
	 * @throws IOException
	 *             if the input cannot be properly decompressed
	 */
	public static final byte[] inflate(byte[] in) throws IOException {
		// decompress using InflaterInputStream
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(
				EXPECTED_COMPRESSION_RATIO * in.length);

		InflaterInputStream inStream = new InflaterInputStream(
				new ByteArrayInputStream(in));

		byte[] buf = new byte[BUF_SIZE];
		while (true) {
			int size = inStream.read(buf);
			if (size <= 0)
				break;
			outStream.write(buf, 0, size);
		}
		outStream.close();

		return outStream.toByteArray();
	}

	/***
	 * Returns a deflated copy of the input array.
	 */
	public static final byte[] deflate(byte[] in) {
		// compress using DeflaterOutputStream
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream(in.length
				/ EXPECTED_COMPRESSION_RATIO);

		DeflaterOutputStream outStream = new DeflaterOutputStream(byteOut);

		try {
			outStream.write(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return byteOut.toByteArray();
	}

	public static String unzip(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ZipInputStream in = new ZipInputStream(bis);
		in.getNextEntry();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int nRead = 0;
		while ((nRead = in.read(buf, 0, 4096)) > 0) {
			bos.write(buf, 0, nRead);
		}
		String ret = new String(bos.toByteArray());
		return ret;
	}

	public static byte[] zip(String zipSrc) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bos);
		// 创建ZipOutputStream类对象
		zip(zipSrc, out);// 调用方法
		out.close();// 将流关闭

		return bos.toByteArray();
	}

	private static void zip(String zipSrc, ZipOutputStream out)
			throws IOException {
		out.putNextEntry(new ZipEntry("val"));// 创建新的进入点
		byte[] bytes = zipSrc.getBytes();
		out.write(bytes);
	}
}