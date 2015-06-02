import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sites.Site;
import sites.Site.Listener;
import sites.comprasNet.ComprasNet;
import auction.Auction.AuctionMode;
import config.Config;

class Main {
	private Site site;
	
	static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String args[]) {
		logger.info("Auction Bot started!");
		Main main = new Main();
		main.run();
	}
	
	void run() {
		Listener listener = new Listener() {
			@Override
			public void onPageLoadSuccess() {
				logger.info("Page loaded");
				site.login();
			}

			@Override
			public void onPageLoadFail(String message) {
				logger.error("Page Load error: {}", message);
			}

			@Override
			public void onLoginSuccess() {
				logger.info("Loged in");
				site.openProject("project1");
			}
			
			@Override
			public void onLoginFail(String message) {
				logger.error("Login error: {}", message);
			}
			
			@Override
			public void onOpenProjectSuccess() {
				logger.info("Project opened");
				site.startMonitor();
			}
			
			@Override
			public void onOpenProjectFail(String message) {
				logger.error("Open Project error: {}", message);
			}
			
			@Override
			public void onStartMonitorSuccess() {
				logger.info("Project being monitored from now on");
			}
			
			@Override
			public void onStartMonitorFail(String message) {
				logger.error("Start Monitor error: {}", message);
			}

			@Override
			public void onStopMonitorSuccess() {
				logger.info("Project not being monitored anymore");
			}
			
			@Override
			public void onStopMonitorFail(String message) {
				logger.error("Stop Monitor error: {}", message);
			}
		
			@Override
			public void onMonitorFail(String message) {
				logger.error("Monitor error: {}", message);
			}
			
			@Override
			public void onAuctionStarted() {
				logger.info("Auction started");
			}
			
			@Override
			public void onPlaceBidSuccess(String id, long value) {
				logger.info("BID placed successfully: id: {} - value: {}", id, value);
			}
			
			@Override
			public void onPlaceBidFail(String id, long value, String message) {
				logger.error("BID place error: ID: {} - VALUE: {} - REASON: {}", id, value, message);
			}
			
			@Override
			public void onLeadingBidChanged(boolean ours, long value) {
				logger.info("Leading bid changed: OURS: {} - VALUE: {}", ours, value);
			}
			
			@Override
			public void onAuctionModeChanged(String id, AuctionMode mode) {
				logger.info("Auction mode changed: ID: {} - MODE: {}", id, mode);
			}
			
			@Override
			public void onAuctionEnded() {
				logger.info("Auction ended");
			}
		};
		
		site = new ComprasNet("https://www.comprasnet.gov.br/seguro/loginPortal.asp", "1", listener);

		if (Config.UseValidUserInfo) {
			site.setLogin("ALSTOMTED", "SALESFORCE2015");
		} else {
			site.setLogin("BLA", "BLE");
		}
				
		if (Config.TestMode) {
			site.openProject("FAKE");
		} else {
			site.load();
		}

		
		logger.info("Back to main...");
//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean waitForCommands = true;
		do {
			try {
//				System.out.println("Press enter to quit");
//				String input = reader.readLine();
				String input = getCommand();
				if (input == null) continue;
				
				String command = "";
				int separator = input.indexOf(' ');
				if (separator != -1) {
					command = input.substring(0, separator);
					input = input.substring(separator + 1);
					logger.debug("Command: \"{}\" - Complement: \"{}\"", command, input);
				} else {
					command = input;
					logger.debug("Command: \"{}\"", command);
				}
				
				boolean error = false;
				command.toUpperCase();
				switch (command) {
				case "R":
					logger.info("Refresing...");
					site.refresh();
					break;
					
				case "B":
					if (input.length() > 0) {
						try {
							String []params = input.split(" ");
							if (params.length != 2) {
								throw new InvalidParameterException("Command takes 2 parameters.");
							}
							String id = params[0];
							String valueStr = getMultiplyBy10000(params[1]) ? params[1] + "0000" : params[1];
							long value = Long.parseLong(valueStr);
							logger.info("Placing Bid: id = {}, value = {}", id, value);
							site.placeBid(id, value);
						} catch (NumberFormatException e) {
							logger.error("Invalid parameter.");
							error = true;
						} catch (InvalidParameterException e) {
							logger.error("Invalid parameter: {}", e.getMessage());
							error = true;
						} catch (RuntimeException e) {
							logger.error("Error: {}", e.getMessage());
							error = true;
						}
					} else {
						logger.error("Parameter required.");
						error = true;
					}
					if (error) {
						logger.info("    Syntax: B <id> <value>");
						logger.info("      It will place a Bid.");
					}
					break;
					
				case "L":
					if (input.length() > 0) {
						try {
							String []params = input.split(" ");
							if (params.length != 2) {
								throw new InvalidParameterException("Command takes 2 parameters.");
							}
							String id = params[0];
							String valueStr = getMultiplyBy10000(params[1]) ? params[1] + "0000" : params[1];
							long value = Long.parseLong(valueStr);
							logger.info("Setting Lower Limit to {} for {}", value, id);
							site.setLowerLimit(id, value);
						} catch (NumberFormatException e) {
							logger.error("Invalid parameter.");
							error = true;
						} catch (InvalidParameterException e) {
							logger.error("Invalid parameter: {}", e.getMessage());
							error = true;
						} catch (RuntimeException e) {
							logger.error("Error: {}", e.getMessage());
							error = true;
						}
					} else {
						logger.error("Parameter required.");
						error = true;
					}
					if (error) {
						logger.info("    Syntax: L <id> <value>");
						logger.info("      It will set the Lower Limit.");
					}
					break;
										
				case "U":
					if (input.length() > 0) {
						try {
							String []params = input.split(" ");
							if (params.length != 2) {
								throw new InvalidParameterException("Command takes 2 parameters.");
							}
							String id = params[0];
							String valueStr = getMultiplyBy10000(params[1]) ? params[1] + "0000" : params[1];
							long value = Long.parseLong(valueStr);
							logger.info("Setting Upper Limit to {} for {}", value, id);
							site.setUpperLimit(id, value);
						} catch (NumberFormatException e) {
							logger.error("Invalid parameter.");
							error = true;
						} catch (InvalidParameterException e) {
							logger.error("Invalid parameter: {}", e.getMessage());
							error = true;
						} catch (RuntimeException e) {
							logger.error("Error: {}", e.getMessage());
							error = true;
						}
					} else {
						logger.error("Parameter required.");
						error = true;
					}
					if (error) {
						logger.info("    Syntax: U <id> <value>");
						logger.info("      It will set Higher Limit.");
					}
					break;
										
				case "T":
					if (input.length() > 0) {
						try {
							int value = Integer.parseInt(input);
							logger.info("Setting Refresh Rate to {}", value);
							site.setRefreshRate(value);
						} catch (NumberFormatException e) {
							logger.error("Invalid parameter.");
							error = true;
						} catch (RuntimeException e) {
							logger.error("Error: {}", e.getMessage());
							error = true;
						}
					} else {
						logger.error("Parameter required.");
						error = true;
					}
					if (error) {
						logger.info("    Syntax: T <value>");
						logger.info("      It will set Refresh Rate.");
					}
					break;
										
				case "Q":
					logger.info("Quit requested.");
					waitForCommands = false;
					break;
				
				case "?":
				default:
					logger.error("Invalid command.");
					logger.info("\tR                     Refresh");
					logger.info("\tB <id> <value>        Place a bid.");
					logger.info("\tL <id> <value>        Set lower limit.");
					logger.info("\tH <id> <value>        Set higher limit.");
					logger.info("\tT <value>             Adjust refresh rate.");
					logger.info("\tQ                     Quit.");
					break;
				}
//			} catch (IOException e) {
//				logger.error("Error reading from console.", e);
			} catch (Exception e) {
				logger.error("Error reading or executing command.", e);
			}
		} while (waitForCommands);

		logger.info("Quitting...");
		site.close();
	}
	
	String getCommand() {
		return (String)JOptionPane.showInputDialog(
		                    null, //frame,
		                    "R                                 Refresh\n" +
		                    "B <id> <value>        Place a bid.\n" + 
							"L <id> <value>        Set lower limit.\n" +
							"H <id> <value>        Set higher limit.\n" +
							"T <value>                 Adjust refresh rate.\n" +
							"Q                                Quit.\n",
		                    "Type a command",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null, // icon,
		                    null, // possibilities,
		                    "Q"); // default
	}

	boolean getMultiplyBy10000(String value) {
		return 0 == JOptionPane.showConfirmDialog(
			    null, // frame,
			    "You typed: " + value + "\n\n" +
			    "Attention, values are in 1/1000 of a currency unit.\n" +
			    "Multiply it automatically by 10000?",
			    "Be carefull with cents!!",
			    JOptionPane.YES_NO_OPTION);
	}
}
