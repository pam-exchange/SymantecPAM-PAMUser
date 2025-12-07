# Connector for PAM Users

The Symantec PAM User connector is used to manage passwords for PAM login users.  
Think of PAM as an endpoint where the target account is a PAM login user.

The `PAM User` connector covers two scenarios.  
In addition to managing a local PAM user, there is also an example showing how to automate login to PAM using both a local PAM user and an Active Directory user.

Quick jump to detailed descriptions:

- [PAM User for Breakglass](/docs/PAMUser-Breakglass.md)
- [PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md)
- [Automated Login to PAM GUI](/docs/PAMUser-Login.md)

## Breakglass Account for Local PAM User

In this scenario, a local PAM user acts as a breakglass account managed by PAM itself.  
The user exists in PAM, and PAM manages the account’s password.  
If you have an existing breakglass process used to extract and store passwords for breakglass accounts, the local breakglass store can also hold the password of a local PAM user to enable emergency login to PAM.

Such a breakglass account typically has a Global Administrator role, but it may be any role required for emergency access.

Detailed setup and configuration are described in  
**[PAM User for Breakglass](/docs/PAMUser-Breakglass.md)**.

## Manage Local PAM User in a Different PAM Environment

This scenario involves two independent PAM environments.  
They are not part of the same cluster; instead, they are two separate PAM servers or clusters with no connection between them.

The idea is to have a PAM administrator account in one environment managed by the other environment.  
If login to the PAM GUI is automated, a standard user in one environment can log in to the other environment as an administrator **without knowing the administrator account password**.

Detailed setup and configuration are described in  
**[PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md)**.

## Automated Login to PAM

A description of how to configure PAM for automated login to the PAM GUI is also available.  
This works with managed local PAM users as well as Active Directory users.  
Regardless of the authentication type, automated GUI login is supported.

Detailed setup and configuration are described in  
**[Automated Login to PAM GUI](/docs/PAMUser-Login.md)**.

# Build the PAMUser Connector

## Environment

The environment used for building and testing:

- CentOS 9 (with SELinux) for TCF server
- Java JDK 17.0.12
- Apache Tomcat 10.1.30
- Apache Maven 3.9.9
- Symantec PAM 4.2.3
- capamextensioncore 4.21.0.82

## Installation

1. Download the project sources from GitHub.
2. Add the `capamextensioncore.jar` from Symantec PAM to your local Maven repository.
3. Edit `pamuser_messages.properties` and `PAMUserMessageConstants.java`  
   and adjust the message numbers so they do not conflict with other TCF connectors.
4. Run `mvnw package` to compile the connector.
5. Copy the resulting `pamuser.war` file to the Tomcat `webapps_targetconnector` directory.
6. (Recommended) Enable logging for this connector by adding the following to Tomcat’s `logging.properties`:  
```logging.properties
#
# Target Connectors
#
ch.pam_exchange.pam_tc.pamuser.api.level = FINE
ch.pam_exchange.pam_tc.pamuser.api.handlers= java.util.logging.ConsoleHandler
```

# PAM setup and configuration

Detailed documentation for PAM setup and configuration is found in the links above..


# Version history

1.0.0  
- Initial release
