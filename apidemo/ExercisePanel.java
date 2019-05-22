/* Copyright (C) 2019 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package apidemo;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import client.Types.ExerciseType;
import client.Types.SecType;
import controller.ApiController.IAccountHandler;
import controller.Position;

import apidemo.AccountInfoPanel.PortfolioModel;
import util.HtmlButton;
import util.NewTabbedPanel.INewTab;
import util.TCombo;
import util.UpperField;
import util.VerticalPanel;
import util.VerticalPanel.HorzPanel;


public class ExercisePanel extends HorzPanel implements INewTab, IAccountHandler {
	private DefaultListModel<String> m_acctList = new DefaultListModel<>();
	private JList<String> m_accounts = new JList<>( m_acctList);
	private String m_selAcct = "";
	private PortfolioModel m_portfolioModel = new PortfolioModel();
	private JTable m_portTable = new JTable( m_portfolioModel);
	
	ExercisePanel() {
		JScrollPane acctsScroll = new JScrollPane( m_accounts);
		acctsScroll.setBorder( new TitledBorder( "Select account"));
		
		JScrollPane portScroll = new JScrollPane( m_portTable);
		portScroll.setBorder( new TitledBorder( "Select long option position"));
		
		setLayout( new BoxLayout( this, BoxLayout.X_AXIS) );
		add( acctsScroll);
		add( portScroll);
		add( new ExPanel() );

		m_accounts.addListSelectionListener(e -> onAcctChanged());
	}
	
	private void onAcctChanged() {
		int i = m_accounts.getSelectedIndex();
		if (i != -1) {
			String selAcct = m_acctList.get( i);
			if (!selAcct.equals( m_selAcct) ) {
				m_selAcct = selAcct;
				m_portfolioModel.clear();
				ApiDemo.INSTANCE.controller().reqAccountUpdates(true, m_selAcct, this);
			}
		}
	}

	class ExPanel extends VerticalPanel {
		TCombo<ExerciseType> m_combo = new TCombo<>( ExerciseType.values() );
		UpperField m_qty = new UpperField( "1");
		JCheckBox m_override = new JCheckBox();
		
		ExPanel() {
			HtmlButton but = new HtmlButton( "Go") {
				protected void actionPerformed() {
					onExercise();
				}
			};
			
			m_combo.removeItem( ExerciseType.None);
			
			add( "Action", m_combo);
			add( "Quantity", m_qty);
			add( "Override", m_override);
			add( but);
		}

		void onExercise() {
			String account = m_accounts.getSelectedValue();
			int i = m_portTable.getSelectedRow();
			if (i != -1 && account != null) {
				Position position = m_portfolioModel.getPosition( i);
				ApiDemo.INSTANCE.controller().exerciseOption(account, position.contract(), m_combo.getSelectedItem(), m_qty.getInt(), m_override.isSelected() );
			}
		}
	}

	/** Show long option positions only. */
	@Override public void updatePortfolio(Position position) {
		if (position.account().equals( m_selAcct) && position.contract().secType() == SecType.OPT) {		
			m_portfolioModel.update( position);
		}
	}
		
	/** Called when the tab is first visited. */
	@Override public void activated() {
		for (String account : ApiDemo.INSTANCE.accountList() ) {
			m_acctList.addElement( account);
		}
	}
	
	/** Called when the tab is closed by clicking the X. */
	@Override public void closed() {
	}

	@Override public void accountValue(String account, String key, String value, String currency) {
	}

	@Override public void accountTime(String timeStamp) {
	}

	@Override public void accountDownloadEnd(String account) {
	}
}
