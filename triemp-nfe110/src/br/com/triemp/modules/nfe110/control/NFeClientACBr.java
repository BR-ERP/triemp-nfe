package br.com.triemp.modules.nfe110.control;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.freedom.library.functions.Funcoes;

/**
 * 
 * @author paulo
 *
 * Classe responsavel por fazer a comunicação com o ACBrNFeMonitor
 */

public class NFeClientACBr {
	private String host;
	private int port;
	private int timeout;
	private Socket socket;
	private PrintStream printStream;
	private NFeClientThread nfeClientThread;
	
	public NFeClientACBr(String host, int port){
		this(host, port, 2);
	}
	
	public NFeClientACBr(String host, int port, int timeout){
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}
	
	public boolean conectar(){
		try {
			this.socket = new Socket(this.host, this.port);
			this.printStream = new PrintStream(this.socket.getOutputStream());		
			nfeClientThread = new NFeClientThread(this.socket);
			new Thread(nfeClientThread).start();
			this.getRetornoOperacao(0);
			return true;
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "Não foi possível conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Não foi possível conectar no host "+this.host, "Falha ao conectar", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	public String getStatusServico(){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.STATUSSERVICO");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String assinarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ASSINARNFE(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String validarNFe(String file){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.VALIDARNFe(\""+file+"\")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String enviarNFe(String file, String lote, boolean assina, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIARNFE(\""+file+"\","+lote+","+(assina?"1":"0")+","+(danfe?"1":"0")+")");
		printStream.println(".");
		StringBuffer retorno = new StringBuffer(this.getRetornoOperacao(0));
		while((retorno.indexOf("OK:") == -1) && (retorno.indexOf("ERRO:")  == -1)){
			retorno.append(this.getRetornoOperacao(1));
		}
		String ret = null;
		if(retorno.indexOf("OK:") != -1){
			do{
				retorno.append(this.getRetornoOperacao(1));
			}while(retorno.indexOf("[RETORNO]") == -1);
			if(retorno.indexOf("CStat=100") != -1){
				ret = "OK";
			}else if(retorno.indexOf("XMotivo=Rejeição") != -1){
				ret = retorno.substring(retorno.indexOf("XMotivo=Rejeição"), retorno.length());
				ret = ret.substring(0, ret.indexOf("CUF="));
				ret = ret.replace("XMotivo=", "");
			}
		}else if(retorno.indexOf("ERRO:") != -1){
			ret = "Erro desconhecido ao tentar enviar arquivo da NF-e.";
		}
		return ret;
	}
	
	public String enviarEmail(String email, String file, boolean danfePdf){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.ENVIAREMAIL(\""+email+"\",\""+file+"\","+(danfePdf?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String cancelarNFe(String chave, String justificativa){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CANCELARNFE(\""+chave+"\",\""+justificativa+"\"");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String criarNFe(String file, boolean retornaXML){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARNFE(\""+file+"\","+(retornaXML?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	public String criarEnviarNFe(String file, String lote, boolean danfe){
		this.nfeClientThread.resetStringBuffer();
		printStream.println("NFE.CRIARENVIARNFE(\""+file+"\","+lote+","+(danfe?"1":"0")+")");
		printStream.println(".");
		return this.getRetornoOperacao(timeout);
	}
	
	private String getRetornoOperacao(int seg){
		String ret;
		Funcoes.espera(seg);
		do{
			ret = this.nfeClientThread.getStringBuffer().toString();
		}while (ret.equals(""));
		return ret.replaceAll("", "");
	}
	
	public String getRetorno(){
		return this.nfeClientThread.getStringBuffer().toString().replaceAll("", "");
	}
	
	public void close(){
		if(this.nfeClientThread != null){
			this.nfeClientThread.stop();
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected class NFeClientThread implements Runnable {
		private Scanner scanner;
		private StringBuffer stringBuffer;
		private boolean run;
		
		public NFeClientThread(Socket socket){
			try {
				scanner = new Scanner(socket.getInputStream(), "ISO-8859-1");
				this.stringBuffer = new StringBuffer();
				this.run = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run() {
			while(scanner.hasNextLine() && this.run){
				stringBuffer.append(scanner.nextLine()+"\n");
			}
		}
		public void stop(){
			this.run = false;
		}
		public String getStringBuffer(){
			return this.stringBuffer.toString();
		}
		public void resetStringBuffer(){
			this.stringBuffer = new StringBuffer();
		}
	}
}