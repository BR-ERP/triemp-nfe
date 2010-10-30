/**
 * @version 25/05/2010 
 * @author Triemp Solutions / Paulo Bueno <BR>
 * 
 * Projeto: Triemp-nfe <BR>
 * 
 * Pacote: br.com.triemp.modules.nfe <BR>
 * Classe: @(#)TriempNFEFactory.java <BR>
 * 
 * Este arquivo é parte do sistema Freedom-ERP, o Freedom-ERP é um software livre; você pode redistribui-lo e/ou <BR>
 * modifica-lo dentro dos termos da Licença Pública Geral GNU como publicada pela Fundação do Software Livre (FSF); <BR>
 * na versão 2 da Licença, ou (na sua opnião) qualquer versão. <BR>
 * Este programa é distribuido na esperança que possa ser  util, mas SEM NENHUMA GARANTIA; <BR>
 * sem uma garantia implicita de ADEQUAÇÂO a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. <BR>
 * Veja a Licença Pública Geral GNU para maiores detalhes. <BR>
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU junto com este programa, se não, <BR>
 * de acordo com os termos da LPG-PC <BR>
 * <BR>
 */
package br.com.triemp.modules.nfe110.control;

import org.freedom.modules.nfe.control.AbstractNFEFactory;

public class TriempNFe extends AbstractNFEFactory {
	private NFe nfe;
	@Override
	protected void validSend() {
		createNFEXmlFile();
	}
		
	protected void createNFEXmlFile() {
		//DLLoading loading = new DLLoading();
		//loading.start();
		if(getTpNF().equals(AbstractNFEFactory.TP_NF_IN)){		 // 0 - Entrada
			nfe = new NFeCompra(this.getConSys(), this.getConNFE(), this.getKey());
		}else if(getTpNF().equals(AbstractNFEFactory.TP_NF_OUT)){// 1 - Saida
			nfe = new NFeVenda(this.getConSys(), this.getConNFE(), this.getKey());
		}
		
		if(nfe != null){
			getKey().put("CHAVENFE", nfe.infNFe.getId().replace("NFe", ""));
		}
		//loading.stop();
		nfe = null;
	}
	
	public void cancelarNFe(){
		/**
		 * @author Paulo Bueno
		 * Comunicação com ACBrNFeMonitor, para assinar, validar, transmitir a NF-e e enviar e-mail para o destinatário.
		 */
		/*
		if(Aplicativo.getParameter("srvnfe").equals("S")){
			String retorno = null;
			StringBuffer mensagem = new StringBuffer();
			File xmlTemp = null;
			NFeClientACBr nfeClient = new
			NFeClientACBr(Aplicativo.getParameter("ipservnfe"), Integer.valueOf(Aplicativo.getParameter("portservnfe")));
			try{
				if(nfeClient.conectar()){
					if(nfeClient.getStatusServico().indexOf("OK") != -1){
						mensagem.append("Verificando status do serviço - OK\n");

						retorno = nfeClient.enviarNFe(pathnfe, ide.getNNF(), true, true);
						
						if(retorno.indexOf("OK") != -1){
							mensagem.append("Enviando arquivo da NF-e - OK\n");
		*/
	}

	@Override
	protected void runSend() {}
}
