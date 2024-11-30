# StockSim Overview

> [!WARNING]
>
> This documentation version maybe deprecated.
>
> Please review the `README.md` file in the root directory for latest updates.

Within our project documentation, the features in black are the basic
functionalities we need to implement first. These are our priorities and must be
fully functional for our initial version. The features in gray are advanced
options that we can consider adding later. These advanced features will enhance
our app's functionality and user experience but are not critical for the initial
launch. The first three pages explain briefly how the app works. Starting on
page 4, we will provide detailed descriptions of the mechanisms for each part.

## Users' Information

Users should **register** (new users) or **log in**.

Once they have an account, new users are given an initial **balance** of
$100,000, while the balance for returning users will be loaded based on the last
recorded state from their previous session.

### Registration

New users will create an account by providing a Trader ID, a password
(optional), and an email. They will also select a difficulty level for their
trading simulation (Beginner, Intermediate, Advanced), which will determine the
initial balance. An email verification process can be implemented to validate
the account.

### Login

Returning users can log in using their trader ID/email and password. If they
forget their password, they can reset it.

### Balances

Initial Balance: Depending on the difficulty level chosen during registration,
the initial balance will vary. For instance:

- Beginner: $50,000
- Intermediate: $100,000
- Advanced: $150,000

For returning users, their balance will be loaded based on the last recorded
state from their previous session. The balance must be greater than 0. If the
balance (initial money - loss) < 0, then the game is over. We will delete all
the user information, and the user will have to create a new account. We'll
explain this in detail later.

## Making Trades

**Order Entry (Buy/Sell)**: When users click on 'Order Entry,' a trading window
will appear.

They can place **market orders** by selecting a stock (**Ticker**) from the
three stocks provided, entering the **quantity**, and choosing to 'Buy' or
'Sell' to execute the trade.

### Buy/Sell

Buy (`+`): This is when you purchase shares of a stock. When the stock price
goes up, you can sell it later at a higher price.

Sell (`-`): This usually happens when you already have some shares of the stock.
Normally, the maximum quantity you can sell cannot exceed the current quantity
you are holding.

However, there is a special case of selling called short-selling. Even if you
don't own any shares of a stock, you can temporarily 'borrow' some shares from
others and sell them. Later, you have to buy back the shares you borrowed. If we
don't allow short-selling, we need to make the position of a stock always >= 0
(Buy >= Sell).

There are typically two types of orders:

- Market orders: Users can place market orders by selecting a stock (Ticker),
  entering the quantity, and choosing 'Buy' or 'Sell'. Orders will be executed
  immediately at the current market price, so users cannot choose the price
  (that's why it looks darker).
- ~~Limit Orders: Users can specify the price at which they are willing to buy
  or sell a stock. The order will only execute if the market price meets or
  exceeds this limit.~~

For the sake of simplicity in our initial implementation, we'll only be using
market orders. This means we won't need to handle the logic of checking whether
trades are successful based on price and volume conditions. Instead, all trades
will execute immediately at the current market price, ensuring that every
transaction is completed.

### Ticker

To simplify our initial implementation, we'll limit the range of stocks
available for trading to a few well-known companies, such as Apple, Amazon, and
Tesla. We might also limit the number of different stocks each user can hold
(maybe 3).

We might enable users to enter any stock ticker later. This feature would
involve integrating a search and validation mechanism to ensure the ticker is
valid and available in the market data we have access to with our API key.

## Checking Current Positions

### Portfolio

When a user clicks on 'Portfolio', a window will appear showing all the stocks
they are currently holding, which helps traders in decision-making. The window
includes:

- Buy/Sell Price: The price at which the user bought or sold the stock
- Current Market Price: The latest market price of the stock
- Quantity: The number of shares the user owns
- Profit/Loss per Share: The gain or loss per share, calculated as the
  difference between the current market price and the buy/sell price
- Total Profit/Loss: The overall gain or loss, which is the profit/loss per
  share multiplied by the quantity of shares owned

## Balance Check

Continuously monitor the user's balance after every transaction (buy or sell).
Implement a function that checks the balance every time a transaction is
processed.

### Game Over Condition

If a balance check reveals that the balance has fallen below zero, trigger a
game over condition. This condition could be set by a simple conditional
statement in the transaction processing logic:
`if (currentBalance < 0) { triggerGameOver(); }`.

### Game Over Functionality

Define a function `triggerGameOver()` that handles the termination of the
account. This function could log the user out, display a message indicating that
the game is over due to insufficient funds, and optionally delete or deactivate
the user's account, depending on your requirements.

### User Feedback

Provide immediate feedback to the user through a pop-up message or a screen
redirect explaining why the game ended.

### Database Update

Update the database to reflect the account's status as terminated, ensuring that
the user cannot log in unless they start a new game.

---

## Use Cases and User Stories

### Use Case 1: User Registration

User Story: As a new user, I want to create a new account so that I can log into
the app and start trading. To sign up/register, I need to choose a userID and a
password.

Steps:

- The user selects "Sign Up" and types in the required information (View ->
  Controller).
- The RegistrationController loads the registration info to the Interactor. The
  Interactor verifies data compliance and creates a new account (Controller ->
  Interactor).
- If registration is successful, the RegistrationInteractor instructs the
  RegistrationView to display a success message and guide the user to log in
  (Controller -> Presenter).
- If registration fails, the RegistrationController instructs the
  RegistrationView to display an error message (Controller -> Presenter).

### Use Case 2: User Login

User Story: As a returning user, I want to log into my account with my username
and password (credentials) so that I can resume all trading activities, balance,
etc.

Steps:

- The user inputs their login information (username and password) and submits
  (View -> Controller).
- The LoginController verifies credentials and processes the login (Interactor).
- If the login is successful, the LoginInteractor loads the user data and
  directs the LoginView to the trading interface (Interactor -> Presenter).
- If the login fails, the LoginInteractor instructs the LoginView to display an
  error message (Interactor -> Presenter).

### Use Case 3: Execute Trades BUY

User Story: As a user, I want to buy shares of a stock so that I can hold a long
position in it. For example, I can buy 10 shares of AAPL (Apple Inc.) at $5 per
share.

Steps:

- The user selects a ticker (symbol of the stock) and quantity to buy, and
  submits a buy order in the trading window (Controller -> Interactor).
- The ExecuteBuyInteractor processes the order and updates the account balance
  and portfolio (Interactor).
- The ExecuteBuyInteractor returns the trade results to the TradeView,
  displaying success or error messages (Interactor -> Presenter).

### Use Case 4: Execute Trades SELL

User Story: As a user, I want to sell shares of a stock that I am currently
holding so that the position of the stock will decrease and I will get money in
return. For example, I have 10 shares of AAPL (Apple Inc.), so I can sell 5
shares.

Steps:

- The user selects a ticker (symbol of the stock) and quantity to sell, and
  submits a sell order in the trading window (Controller -> Interactor).
- The ExecuteSellInteractor processes the order and updates the account balance
  and portfolio (Interactor).
- The ExecuteSellInteractor returns the trade results to the TradeView,
  displaying success or error messages (Interactor -> Presenter).

### Use Case 5: Execute Trades SHORTSELL

User Story: As a user, I am not holding any shares of AAPL (Apple). I want to
short-sell 20 shares of Apple so that I can earn money. Even though my current
position in Apple is 0, I can 'borrow' shares of Apple from someone else and
sell the borrowed shares.

Steps:

- The user selects a ticker (symbol of the stock) and quantity to short-sell,
  and submits a sell order in the trading window (Controller -> Interactor).
- The ExecuteShortSellInteractor processes the order, updating the account
  balance and portfolio (Interactor).
- The ExecuteShortSellInteractor returns the trade results to the TradeView,
  displaying success or error messages (Interactor -> Presenter).

### Use Case 6: View Portfolio

User Story:

- As a trader, I want to access my current stock holdings and check bought
  price, quantity, current market price, profit/loss per share, and total
  profit/loss so I can make better trades.
- (Optional) As a trader, I want to sort by column and filter my current stock
  holdings to display select rows and quantities so I can view certain trades.

Steps:

- The user selects a page and/or rows per page for pagination-based item
  selection from the selection provided near the bottom (Interactor ->
  Controller).
- The PortfolioController filters all user trades to the select rows, quantity,
  and column sort (Controller).
- The PortfolioController returns the filtered data to the PortfolioView,
  displaying the new view of current stock holdings and its respective
  information.

### Use Case 7: View Transaction History

User Story:

- As a trader, I want to access my transaction history and check bought and/or
  sold price, quantity, current market price, profit/loss per share, total
  profit/loss, and [Optional for sold stocks] current mp profit/loss per share,
  current mp total profit/loss so I can learn from past trades.
- (Optional) As a trader, I want to sort by column and filter my current stock
  holdings to display select rows and quantities so I can view certain trades.

Steps:

- The user selects a page and/or rows per page for pagination-based item
  selection from the selection provided near the bottom (Interactor ->
  Controller).
- The HistoryController filters all user trades to the select rows, quantity,
  and column sort (Controller).
- The HistoryController returns the filtered data to the HistoryView, displaying
  the new view of current stock holdings and its respective information.

### Use Case 8: View Ranking

User Story: As a trader, I want to view my rank compared to other traders based
on portfolio performance (portfolio values) so I can assess my relative
performance and strive to improve.

Steps:

- The user selects a page and/or rows per page for pagination-based item
  selection from the selection provided near the bottom (Interactor ->
  Controller).
- The RankingController retrieves the current user's rank by comparing their
  portfolio's total value with other users and formats all users' assets and
  portfolio values by ranking.
- The RankingController sends the data to the RankingView and provides optional
  details, such as viewing the top 3 or top 5 users listed in descending order.

### Use Case 9: Password Reset + Email Verification

User Story: As a trader, I want to reset my password securely using email
verification so I can regain access to my account if I forget my password.

Steps:

- The user selects "ForgotPassword" and enters their registered email address
  (Interactor -> Controller).
- The PasswordResetController verifies if the email exists in the system and, if
  it exists, generates a one-time link that allows the user to reset the
  password.
- The PasswordResetController sends an email to the user's registered email
  address with the one-time link using a service like JavaMail API (optional).
- The user clicks the one-time link, and the PasswordResetController checks if
  the one-time link is still valid (within the validity date). If the
  verification is successful, the PasswordResetView prompts the user to enter a
  new password and confirm it.
- The PasswordResetController validates the new password (checking if it
  contains the minimum number of characters, length, or uppercase requirements)
  and updates it in the system.
- The PasswordResetView confirms the successful password reset and directs the
  user back to the login screen.

### Use Case 10: Feedback/Support

User Story: As a user, I want to report technical issues or provide feedback so
I can communicate with the support team if I encounter issues or have
suggestions.

Steps:

- The user navigates to the "Feedback/Support" section in the application.
- The user submits a report ticket or feedback message, which can include
  details like issue types, description, and optional attachments (Interactor ->
  Controller).
- FeedbackController validates the user's message content (checks for empty
  fields or inappropriate content). If valid, FeedbackController saves the
  feedback or support request in the database for tracking and management and
  sends an acknowledgment email to the user.
- FeedbackView confirms the successful submission and provides the user with a
  ticket ID for future reference.

### Use Case 11: Minimum Number of Trades

User Story: As a trader, I want to be aware of my total number of trades to
ensure I meet the minimum trading requirements for account benefits or
eligibility for promotions.

Steps:

- The trader selects "View Trade Activity" to view their trade count and check
  their progress toward any trade-related requirements.
- The TradeRequirementController retrieves the user's trade history from the
  database and counts the current total number of trades.
- The TradeRequirementController checks this total against the minimum threshold
  or other requirements, such as qualifying for promotions or eligibility.
- The TradeRequirementController sends the data to the TradeRequirementView and
  displays the user's trade count, progress toward the minimum trade
  requirement, and any relevant messages (e.g., "You need 5 more trades to
  qualify").
