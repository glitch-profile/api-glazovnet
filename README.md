** THIS API IS MADE AS A GRADUATION PROJECT AND HAS NO RELATION TO THE PROVIDER GLAZOV.NET **

API developed to work with the personal account of the Internet provider Glazov.Net to use a mobile application.

API is built in Kotlin language based on KTOR framework. Dependencies injection is performed using Koin library. MongoDB database is used for data storage. There is also support for Firebase Cloud Messaging for sending PUSH notifications.

Authentication separates company clients and employees. For employees there are special roles that define what functionality they have access to.

The whole list of supported functionality:
- Authentication 
- News management
- Connection of tariffs and additional services
- Creating requests to tech support and chat for each request (WebSockets)
- Sending announcements to specific addresses or for specific clients
- Adding service news for company employees
- Managing mailings for each customer and sending PUSH notifications
- Daily debiting of customers' accounts for the use of services
- Transaction history for each user with additional description for each transaction
