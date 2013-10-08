/**
 * shifu
 * A application to solve conversion, arithmetic and logical challenges. For the 
 * current version and more informations visit <https://github.com/k42b3/shifu>
 * 
 * Copyright (c) 2013 Christoph Kappestein <k42b3.x@gmail.com>
 * 
 * This file is part of Shifu. Shifu is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or at any later version.
 * 
 * Shifu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Shifu. If not, see <http://www.gnu.org/licenses/>.
 */

package com.k42b3.shifu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Memory
 *
 * @author  Christoph Kappestein <k42b3.x@gmail.com>
 * @license http://www.gnu.org/licenses/gpl.html GPLv3
 * @link    http://shifu.k42b3.com
 */
public class Memory extends JTabbedPane
{
	private int maxInteger = 8;
	private int waitTime = 1;

	private String challenge;
	private String answer;
	
	private JLabel lblChallenge;
	private JTextField txtAnswer;
	private JButton btnSolve;
	private JButton btnSkip;
	private JButton btnAnswer;
	
	private ArrayList<Integer> list;
	private int pos;

	private Connection connection;
	
	public Memory(Connection connection)
	{
		this.connection = connection;

		this.setFocusable(false);
		this.addTab("Challenge", buildChallengePanel());
		this.addTab("Settings", buildSettingPanel());

		// build first challenge
		this.nextChallenge();
	}

	protected void nextChallenge()
	{
		list = new ArrayList<Integer>();
		pos = 0;

		int size = this.getRandomInt();
		StringBuilder answer = new StringBuilder();
		
		for(int i = 0; i < size; i++)
		{
			int num = (int) (10 * Math.random());

			list.add(num);
			answer.append(num);
		}

		setAnswer(answer.toString());

		txtAnswer.setText("");
		txtAnswer.setEnabled(false);

		lblChallenge.setText("Remember ...");

		Timer timer = new Timer(waitTime * 1000, new ActionListener(){

			public void actionPerformed(ActionEvent e) 
			{
				if(pos < list.size())
				{
					String text = "";
					if(!lblChallenge.getText().equals("Remember ..."))
					{
						for(int i = 0; i < lblChallenge.getText().length(); i++)
						{
							text+= "x";
						}
					}

					lblChallenge.setText(text + list.get(pos));

					pos++;
				}
				else
				{
					((Timer) e.getSource()).stop();

					txtAnswer.setEnabled(true);
					txtAnswer.requestFocusInWindow();
					lblChallenge.setText("Answer");
				}
			}

		});

		timer.start();
	}
	
	protected void solveChallenge()
	{
		String answerExpr = txtAnswer.getText();
		
		// check answer
		int correct = 0;

		if(answerExpr.length() == list.size())
		{
			correct = 1;

			for(int i = 0; i < answerExpr.length(); i++)
			{
				if(Integer.parseInt("" + answerExpr.charAt(i)) != list.get(i))
				{
					correct = 0;
					break;
				}
			}
		}
		else
		{
			correct = 0;
		}

		// insert answer
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Statement stmt = connection.createStatement();
			String sql = "INSERT INTO results (`challenge`, `answer`, `correct`, `date`) ";
			sql+= "VALUES ('" + answer + "', " + answerExpr + ", " + correct + ", '" + sdf.format(new Date()) + "')";

			stmt.executeUpdate(sql);
		}
		catch(SQLException e)
		{
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		if(correct == 1)
		{
			txtAnswer.setBackground(Color.GREEN);

			nextChallenge();
		}
		else
		{
			txtAnswer.setBackground(Color.RED);
		}
	}

	protected void answerChallenge()
	{
		txtAnswer.setText(this.answer);
	}
	
	protected void setChallenge(String challenge)
	{
		this.challenge = challenge;
		
		lblChallenge.setText(challenge);
	}
	
	protected void setAnswer(String answer)
	{
		this.answer = answer;
	}
	
	protected int getRandomInt(int max)
	{
		return (int) (Math.random() * max);
	}

	protected int getRandomInt()
	{
		int i = 0;
		while(i == 0)
		{
			i = getRandomInt(getMaxInt());
		}

		return i;
	}

	protected int getMaxInt()
	{
		return maxInteger;
	}

	
	protected Component buildChallengePanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		
		// task
		lblChallenge = new JLabel("-");
		lblChallenge.setFont(new Font("Arial", Font.BOLD, 24));
		lblChallenge.setBorder(new EmptyBorder(8, 8, 8, 8));
		
		panel.add(lblChallenge, BorderLayout.NORTH);
		
		// answer
		txtAnswer = new JTextField();
		txtAnswer.setFont(new Font("Arial", Font.BOLD, 24));
		txtAnswer.setBorder(new EmptyBorder(8, 8, 8, 8));
		txtAnswer.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					solveChallenge();
				}
			}

			public void keyReleased(KeyEvent e)
			{
			}

			public void keyTyped(KeyEvent e)
			{
			}
			
		});
		
		panel.add(txtAnswer, BorderLayout.CENTER);
		
		// buttons
		JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		btnSolve = new JButton("Solve");
		btnSolve.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				solveChallenge();
			}
			
		});
		
		btnSkip = new JButton("Skip");
		btnSkip.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				nextChallenge();
				
				// reset bg color
				txtAnswer.setBackground(new JTextField().getBackground());
			}
			
		});
		
		btnAnswer = new JButton("Answer");
		btnAnswer.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				answerChallenge();
			}
			
		});
		
		panelButtons.add(btnSolve);
		panelButtons.add(btnSkip);
		panelButtons.add(btnAnswer);
		
		panel.add(panelButtons, BorderLayout.SOUTH);
		
		return panel;
	}
	
	protected Component buildSettingPanel()
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setPreferredSize(new Dimension(300, 600));
		
		JPanel panelDifficulty = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblDifficulty = new JLabel("Difficulty");
		lblDifficulty.setPreferredSize(new Dimension(100, 24));

		JSlider sldDifficulty = new JSlider(JSlider.HORIZONTAL, 8, 128, maxInteger);
		sldDifficulty.setPreferredSize(new Dimension(200, 50));
		sldDifficulty.setMajorTickSpacing(20);
		sldDifficulty.setPaintTicks(true);
		sldDifficulty.setPaintLabels(true);
		sldDifficulty.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();

			    if(!source.getValueIsAdjusting())
			    {
			    	maxInteger = (int) source.getValue();
			    }
			}

		});

		panelDifficulty.add(lblDifficulty);
		panelDifficulty.add(sldDifficulty);
		
		panel.add(panelDifficulty);
		
		JPanel panelWait = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblWait = new JLabel("Wait");
		lblWait.setPreferredSize(new Dimension(100, 24));

		JSlider sldWait = new JSlider(JSlider.HORIZONTAL, 1, 16, waitTime);
		sldWait.setPreferredSize(new Dimension(200, 50));
		sldWait.setMajorTickSpacing(4);
		sldWait.setPaintTicks(true);
		sldWait.setPaintLabels(true);
		sldWait.addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent e)
			{
				JSlider source = (JSlider) e.getSource();

			    if(!source.getValueIsAdjusting())
			    {
			    	waitTime = (int) source.getValue();
			    }
			}

		});

		panelWait.add(lblWait);
		panelWait.add(sldWait);
		
		panel.add(panelWait);

		return new JScrollPane(panel);
	}
}
