/**
 * @version 25/05/2010 
 * @author Triemp Solutions / Paulo Bueno <BR>
 * 
 * Projeto: Triemp-nfe <BR>
 * 
 * Pacote: br.com.triemp.modules.nfe <BR>
 * Classe: @(#)TriempNFEFactory.java <BR>
 * 
 * Este arquivo � parte do sistema Freedom-ERP, o Freedom-ERP � um software livre; voc� pode redistribui-lo e/ou <BR>
 * modifica-lo dentro dos termos da Licen�a P�blica Geral GNU como publicada pela Funda��o do Software Livre (FSF); <BR>
 * na vers�o 2 da Licen�a, ou (na sua opni�o) qualquer vers�o. <BR>
 * Este programa � distribuido na esperan�a que possa ser  util, mas SEM NENHUMA GARANTIA; <BR>
 * sem uma garantia implicita de ADEQUA��O a qualquer MERCADO ou APLICA��O EM PARTICULAR. <BR>
 * Veja a Licen�a P�blica Geral GNU para maiores detalhes. <BR>
 * Voc� deve ter recebido uma c�pia da Licen�a P�blica Geral GNU junto com este programa, se n�o, <BR>
 * de acordo com os termos da LPG-PC <BR>
 * <BR>
 */
package br.com.triemp.modules.nfe110.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.freedom.infra.functions.SystemFunctions;
import org.freedom.infra.model.jdbc.DbConnection;
import org.freedom.library.functions.Funcoes;
import org.freedom.library.swing.frame.Aplicativo;
import org.freedom.modules.nfe.bean.AbstractNFEKey;
import org.freedom.modules.nfe.bean.FreedomNFEKey;
import org.freedom.modules.nfe.bean.NFEInconsistency;

import br.com.triemp.modules.nfe.util.NFeUtil;
import br.inf.portalfiscal.nfe.ObjectFactory;
import br.inf.portalfiscal.nfe.TEnderEmi;
import br.inf.portalfiscal.nfe.TEndereco;
import br.inf.portalfiscal.nfe.TNFe;
import br.inf.portalfiscal.nfe.TUf;
import br.inf.portalfiscal.nfe.TUfEmi;

public class NFe {

	protected TNFe nfe;
	protected TNFe.InfNFe infNFe;
	protected TNFe.InfNFe.Ide ide;
	protected TNFe.InfNFe.Emit emit;
	protected TEnderEmi endEmit;
	protected TNFe.InfNFe.Dest dest;
	protected TEndereco endDest;
	protected TNFe.InfNFe.Total total;
	protected TNFe.InfNFe.Transp transp;
	protected TNFe.InfNFe.InfAdic infAdic;
	protected List<NFEInconsistency> listInconsistency;
	protected AbstractNFEKey key = null;
	protected DbConnection conSys = null;
	protected DbConnection conNFE = null;
	protected String msgSimples = null;
	protected NFe triempNFe;
	protected String emailNfe = null;
	
	public NFe(DbConnection conSys, DbConnection conNFE, AbstractNFEKey key) {
		this.conSys = conSys;
		this.conNFE = conNFE;
		this.key = key;
		listInconsistency = new ArrayList<NFEInconsistency>();
		nfe = new ObjectFactory().createTNFe();
		infNFe = new ObjectFactory().createTNFeInfNFe();
		nfe.setInfNFe(infNFe);
		ide = new ObjectFactory().createTNFeInfNFeIde();
		infNFe.setIde(ide);
		emit = new ObjectFactory().createTNFeInfNFeEmit();
		infNFe.setEmit(emit);
		endEmit = new ObjectFactory().createTEnderEmi();
		emit.setEnderEmit(endEmit);
		dest = new ObjectFactory().createTNFeInfNFeDest();
		endDest = new ObjectFactory().createTEndereco();
		dest.setEnderDest(endDest);
		infNFe.setDest(dest);
		total = new ObjectFactory().createTNFeInfNFeTotal();
		infNFe.setTotal(total);
		
		carregaPreferenciasNFe();
	}

	protected void carregaInfoNFe() {
		infNFe.setVersao("1.10");
		PreparedStatement ps;
		ResultSet rs;
		String sql = null;
		Integer codigo = null;
		String chaveNFe = null;
		try {
			if(ide != null){
				if(ide.getTpNF().equals("0")){		 // 0 - Entrada
					sql = "SELECT CHAVENFECOMPRA AS CHAVENFE FROM CPCOMPRA WHERE CODEMP=? AND CODFILIAL=? AND CODCOMPRA=?";
					codigo = (Integer) key.get(FreedomNFEKey.CODCOMPRA);
				}else if(ide.getTpNF().equals("1")){// 1 - Saida
					sql = "SELECT CHAVENFEVENDA AS CHAVENFE FROM VDVENDA WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=? AND TIPOVENDA='V'";
					codigo = (Integer) key.get(FreedomNFEKey.CODVENDA);
				}
				ps = conSys.prepareStatement(sql);
				ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
				ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
				ps.setInt(3, codigo);
				rs = ps.executeQuery();
				
				if (rs.next()) {
					chaveNFe = rs.getString("CHAVENFE");
				}
			}
			if(chaveNFe != null){
				infNFe.setId("NFe" + chaveNFe);
				ide.setCNF(getString(chaveNFe.substring(35, 43), 8, true));
				ide.setCDV(getInteger(chaveNFe.substring(chaveNFe.length()-1), 1, true));
			}else{
				Random random = new Random();
				SimpleDateFormat format = new SimpleDateFormat("yyMM");
				String[] dtEmi = ide.getDEmi().split("-");
				String aamm = format.format(new GregorianCalendar(Integer.parseInt(dtEmi[0]), Integer.parseInt(dtEmi[1])-1, Integer.parseInt(dtEmi[2])).getTime());
				String cnpj = NFeUtil.lpad(emit.getCNPJ(), "0", 14);
				String mod = NFeUtil.lpad(ide.getMod(), "0", 2);
				String serie = NFeUtil.lpad(ide.getSerie(), "0", 3);
				String nNf = NFeUtil.lpad(ide.getNNF(), "0", 9);
				String cnf = NFeUtil.lpad(String.valueOf(random.nextInt(99999999)),"0", 9);
				String id = ide.getCUF() + aamm + cnpj + mod + serie + nNf + cnf;
				String cdv = NFeUtil.getDvChaveNFe(id);
				infNFe.setId("NFe" + id + cdv);
				ide.setCNF(getString(cnf, 9, true));
				ide.setCDV(getInteger(cdv, 1, true));
			}
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informa��es da Nota Fiscal!\n" + err.getMessage(), true, conSys, err);
		} catch (Exception err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar informa��es da Nota Fiscal!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}
	
	protected String getFormaPagamento(int codPlanoPag) {
		String indPag = "2";
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT DIASPAG, PERCPAG FROM FNPARCPAG WHERE CODPLANOPAG=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, codPlanoPag);
			rs = ps.executeQuery();

			while (rs.next()) {
				if ((rs.getInt("DIASPAG") == 0) && (rs.getInt("PERCPAG") == 100)) {
					indPag = "0";
				} else if ((rs.getInt("DIASPAG") > 0) && (rs.getInt("PERCPAG") < 100)) {
					indPag = "1";
				}
			}
			
			conSys.commit();
			
		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null, "Erro ao carregar forma de pagamento da venda!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}

		return indPag;
	}

	protected void carregaPreferenciasNFe() {
		PreparedStatement ps;
		ResultSet rs;
		String sql = "SELECT P1.FORMATODANFE, P1.AMBIENTENFE, P1.PROCEMINFE, P1.VERPROCNFE, M.MENS "
					+ "FROM SGPREFERE1 P1 LEFT JOIN LFMENSAGEM M ON (M.CODMENS=P1.CODMENSICMSSIMPLES AND M.CODEMP=P1.CODEMPMS AND M.CODFILIAL=P1.CODFILIALMS AND P1.CREDICMSSIMPLES='S') "
					+ "WHERE P1.CODEMP=? AND P1.CODFILIAL=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setInt(1, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			rs = ps.executeQuery();

			if (rs.next()) {
				ide.setTpImp(getInteger(rs.getString("FORMATODANFE"), 1, true));
				ide.setTpEmis(getInteger("1", 1, true));
				ide.setTpAmb(getInteger(rs.getString("AMBIENTENFE"), 1, true));
				ide.setFinNFe(getInteger("1", 1, true));
				ide.setProcEmi(getInteger(rs.getString("PROCEMINFE"), 1, true));
				ide.setVerProc(getString(rs.getString("VERPROCNFE"), 20, true));
				if(rs.getString("MENS") != null){
					msgSimples = getString(rs.getString("MENS"));
				}
			}
			
			conSys.commit();

		} catch (SQLException err) {
			err.printStackTrace();
			Funcoes.mensagemErro(null,"Erro ao carregar prefer�ncias da NF-e!\n" + err.getMessage(), true, conSys, err);
		} finally {
			rs = null;
			ps = null;
			sql = null;
		}
	}
	
	protected void setInfAdic(String mens){
		if(mens != null){
			if(infAdic == null){
				infAdic = new ObjectFactory().createTNFeInfNFeInfAdic();
				infNFe.setInfAdic(infAdic);
			}
			infAdic.setInfCpl((infAdic.getInfCpl() != null)? infAdic.getInfCpl() + " | " + mens : mens);
		}
	}
	
	protected void setInfAdFisco(String mens){
		if(mens != null){
			if(infAdic == null){
				infAdic = new ObjectFactory().createTNFeInfNFeInfAdic();
				infNFe.setInfAdic(infAdic);
			}
			infAdic.setInfAdFisco((infAdic.getInfAdFisco() != null)? infAdic.getInfAdFisco() + " | " + mens : mens);
		}
	}
	
	protected String gerarXmlNFe(){
		String fileEnviado = "";
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("br.inf.portalfiscal.nfe");
			Marshaller marshaller = jaxbContext.createMarshaller();
			//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, new String("UTF-8"));
			String separador = "";
			if (SystemFunctions.getOS() == SystemFunctions.OS_LINUX) {
				separador = "/";
			} else if (SystemFunctions.getOS() == SystemFunctions.OS_WINDOWS) {
				separador = "\\";
			}
			
			String dirNFe = (String) key.get(FreedomNFEKey.DIRNFE) + separador;
			String[] dir = {dirNFe + "enviar", dirNFe + "temp", dirNFe + "enviado"};
			for(int i=0; i < dir.length; i++){
				File d = new File(dir[i]);
				if(!d.exists() || !d.isDirectory()){
					d.mkdir();
				}
			}
			String nomeXML = infNFe.getId().trim().replace("NFe", "") + "-nfe.xml";
			File xmlNFe = new File(dir[0] + separador + nomeXML);
			FileOutputStream fos = new FileOutputStream(xmlNFe);
			marshaller.marshal(nfe, fos);
			fos.close();

			/**
			 * @author Paulo Bueno
			 * Comunica��o com ACBrNFeMonitor, para assinar, validar, transmitir a NF-e e enviar e-mail para o destinat�rio.
			 */
			if(Aplicativo.getParameter("srvnfe").equals("S")){
				String retorno = null;
				StringBuffer mensagem = new StringBuffer();
				File xmlTemp = null;
				NFeClientACBr nfeClient = new
				NFeClientACBr(Aplicativo.getParameter("ipservnfe"), Integer.valueOf(Aplicativo.getParameter("portservnfe")));
				try{
					if(nfeClient.conectar()){
						String fileTemp = dir[1] + separador + nomeXML;
						xmlTemp = new File(fileTemp);
						if(NFeUtil.copy(xmlNFe, xmlTemp)){
							if(nfeClient.getStatusServico().indexOf("OK") != -1){
								mensagem.append("Verificando status do servi�o - OK\n");
								
								String pathnfe = Aplicativo.getParameter("pathnfe") + nomeXML;
								
								retorno = nfeClient.enviarNFe(pathnfe, ide.getNNF(), true, true);
								
								if(retorno.indexOf("OK") != -1){
									mensagem.append("Enviando arquivo da NF-e - OK\n");
									
									GregorianCalendar data = new GregorianCalendar();
									String pathEnviado = dir[2] + separador + String.valueOf(data.get(Calendar.YEAR)) + separador 
														+ String.valueOf(data.get(Calendar.MONTH)+1);
									File dirEnviado = new File(pathEnviado);
									if(!dirEnviado.exists()){
										dirEnviado.mkdirs();
									}
									
									fileEnviado = pathEnviado + separador + nomeXML;
									File xmlEnviado = new File(fileEnviado);
									if(NFeUtil.copy(xmlTemp, xmlEnviado)){
										xmlNFe.delete();
									}
									
									setChaveNFe();
									
									if(emailNfe != null){
										if(nfeClient.enviarEmail(emailNfe, pathnfe, true).indexOf("OK") != -1){
											mensagem.append("Enviando e-mail da NF-e para o destinat�rio da Nota Fiscal - OK\n");
										}else{
											mensagem.append("Enviando e-mail da NF-e para o destinat�rio da Nota Fiscal - FALHOU\n" + nfeClient.getRetorno() + "\n\n");
										}
									}else{
										mensagem.append("Enviando e-mail da NF-e para o destinat�rio da Nota Fiscal - FALHOU\n e-mail do destinat�rio n�o encontrado!\n\n");
									}
								}else{
									mensagem.append("Enviando arquivo da NF-e - FALHOU\n" + retorno + "\n\n");
								}
							}else{
								mensagem.append("Verificando status do servi�o - FALHOU\n");
							}
						}
					}
				}finally{
					nfeClient.close();
					if(xmlTemp != null){
						xmlTemp.delete();
					}
					if(mensagem.length() > 0){
						JOptionPane.showMessageDialog(null, mensagem, "Envio da NF-e", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			
		}
		return fileEnviado;
	}
	
	protected void setChaveNFe(){
		String sql;
		PreparedStatement ps;
		
		sql = "UPDATE VDVENDA SET CHAVENFEVENDA = ? WHERE CODEMP=? AND CODFILIAL=? AND CODVENDA=? AND TIPOVENDA=?";
		try {
			ps = conSys.prepareStatement(sql);
			ps.setString(1, infNFe.getId().replace("NFe", ""));
			ps.setInt(2, (Integer) key.get(FreedomNFEKey.CODEMP));
			ps.setInt(3, (Integer) key.get(FreedomNFEKey.CODFILIAL));
			ps.setInt(4, (Integer) key.get(FreedomNFEKey.CODVENDA));
			ps.setString(5, "V");
			ps.execute();
			conSys.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String getString(String string) {
		return getString(string, -1);
	}

	protected String getString(String string, int tamanho) {
		return getString(string, tamanho, false);
	}
	
	protected String getString(String string, int tamanho, boolean obrig) {
		return getString(string, tamanho, obrig, null);
	}

	protected String getString(String string, int tamanho, boolean obrig, String str) {
		if (string != null) {
			string = string.trim();
			if(str != null){
				for(int i=0; i < str.length(); i++){
					string = string.replace(String.valueOf(str.charAt(i)), "");
				}
			}
			if (tamanho > 0 && string.length() > tamanho) {
				string = string.substring(0, tamanho);
			}
		} else if (obrig == true) {
			string = "";
		}
		string = removeAcento(string);
		return string;
	}

	protected String getInteger(String valor, int tamanho) {
		return getInteger(valor, tamanho, false);
	}

	protected String getInteger(String valor, int tamanho, boolean obrig) {
		if (valor != null && valor.matches("^[0-9]*[.]{0,1}[0-9]*$")) {
			valor = getDouble(valor.trim(), tamanho, 0);
			//valor = String.valueOf(Integer.valueOf(valor.trim()).intValue());
		} else if (obrig == true) {
			valor = "0";
		}
		return valor;
	}

	protected String getDouble(String valor, int tamanho, int dec) {
		return getDouble(valor, tamanho, dec, false);
	}
	
	protected String getDouble(String valor, int tamanho, int dec, boolean obrig) {
		if (valor != null && valor.matches("^[0-9]*[.]{0,1}[0-9]*$")) {
			BigDecimal bigDecimal = new BigDecimal(valor);
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		} else if (obrig == true) {
			BigDecimal bigDecimal = new BigDecimal("0");
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		}
		return valor;
	}

	protected String getDouble(String valor, int tamanho, int dec, boolean obrig, boolean naoZero) {
		if (valor != null) {
			BigDecimal bigDecimal = new BigDecimal(valor);
			bigDecimal = bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP);
			if(bigDecimal.intValue() == 0 && naoZero){
				valor = null;
			}else{
				valor = String.valueOf(bigDecimal).trim();
			}
		} else if (obrig) {
			BigDecimal bigDecimal = new BigDecimal("0");
			valor = String.valueOf(bigDecimal.setScale(dec, BigDecimal.ROUND_HALF_UP)).trim();
		}
		return valor;
	}

	protected String getDate(String data) {
		return getDate(data, false);
	}

	protected String getDate(String data, boolean obrig) {
		if (data == null && obrig == true) {
			data = Funcoes.dataAAAAMMDD(new Date()).trim();
		}
		return data;
	}

	protected TUf getTUf(String uf) {
		for (TUf tuf : TUf.values()) {
			if (tuf.name().equals(uf)) {
				return TUf.valueOf(uf);
			}
		}
		return null;
	}

	protected TUfEmi getTUfEmi(String uf) {
		for (TUfEmi tuf : TUfEmi.values()) {
			if (tuf.name().equals(uf)) {
				return TUfEmi.valueOf(uf);
			}
		}
		return null;
	}
	
	protected String removeAcento(String string) {
		if(string != null){
			string = string.replaceAll("[�����]","A");  
			string = string.replaceAll("[�����]","a");  
			string = string.replaceAll("[����]","E");  
			string = string.replaceAll("[����]","e");  
			string = string.replaceAll("[����]","I");  
			string = string.replaceAll("[����]","i");  
			string = string.replaceAll("[�����]","O");  
			string = string.replaceAll("[�����]","o");  
			string = string.replaceAll("[����]","U");  
			string = string.replaceAll("[����]","u");  
			string = string.replaceAll("�","C");  
			string = string.replaceAll("�","c");   
			string = string.replaceAll("[��]","y");  
			string = string.replaceAll("�","Y");  
			string = string.replaceAll("�","n");  
			string = string.replaceAll("�","N");  
			string = string.replaceAll("['<>\\|/]","");
		}
		return string;  
	}
}
