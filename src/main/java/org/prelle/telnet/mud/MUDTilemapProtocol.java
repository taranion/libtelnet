package org.prelle.telnet.mud;

import java.io.IOException;
import java.lang.System.Logger.Level;

import org.prelle.telnet.TelnetOptionHandler;
import org.prelle.telnet.TelnetOutputStream;
import org.prelle.telnet.TelnetSocket;

/**
 *
 */
public class MUDTilemapProtocol extends TelnetOptionHandler {

	public final static int CODE = 100;

	public static class TileMapData {
		private int[][] mapData;

		public TileMapData(int w, int h, int[][] mapData) {
			this.mapData = mapData;
		}

		public int[][] getRawData() { return mapData; }
	}

	//-------------------------------------------------------------------
	public MUDTilemapProtocol() {
		super(CODE, "MTP");
	}

	//-------------------------------------------------------------------
	public static void sendMap(TelnetSocket socket, int[][] mapData) throws IOException {
		logger.log(Level.WARNING, "TODO: send map");
		TelnetOutputStream out = (TelnetOutputStream) socket.getOutputStream();

		TelnetOptionHandler.startSubNegotiation(socket, CODE);
		out.write(mapData[0].length);
		out.write(mapData.length);

		for (int y=0; y<mapData.length; y++) {
			for (int x=0; x<mapData[y].length; x++) {
				int code = mapData[y][x];
				out.write(code>>8);
				out.write(code%256);
			}
		}
		TelnetOptionHandler.endSubNegotiation(socket, CODE);
		out.flush();

	}

//	//-----------------------------------------------------------------
//	/**
//	 * @see org.prelle.telnet.TelnetOptionHandler#performSubNegotiation(org.prelle.telnet.TelnetSocket, java.io.InputStream)
//	 */
//	@Override
//	public void performSubNegotiation(TelnetSocket nvt, TelnetInputStream in) throws IOException {
//		in.setHigherLevelControl(true);
//		// MTP Sub negotiation
//		int w = in.read();
//		int h = in.read();
//		int[][] mapData = new int[h][];
//		for (int y=0; y<h; y++) {
//			mapData[y] = new int[w];
//			for (int x=0; x<w; x++) {
//				int high = in.read();
//				int low  = in.read();
//				int v = high*256 + low;
//				mapData[y][x] = v;
//			}
//		}
//
//		logger.log(Level.INFO,"Map: "+ w+"x"+h);
//
////		in.readUntilSE();
//		in.read();
//		in.read();
//		in.setHigherLevelControl(false);
//		logger.log(Level.DEBUG,"Map done");
//
//		nvt.fireOptionDataChanged(this, new TileMapData(w,h,mapData));
//	}

}
