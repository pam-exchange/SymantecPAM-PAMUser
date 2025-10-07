# Connector for PAM Users

The Symantec PAM connector here is used to manage password for PAM login users. Think of PAM as an end-point where the target account is a PAM login user. 

The `PAM User` connector is covering two scenarios. In addition to managing a local PAM user using the `PAM User` connector, there is also an example showing how to automate login to PAM using a local PAM user and an Active Directory user. 

Quick jump to detailed description

- [PAM User for Breakglass](/docs/PAMUser-Breakglass.md)
- [PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md)
- [Automated login to PAM GUI](/docs/PAMUser-Login.md)


## Breakglass account for local PAM user

This scenario is having a local PAM user as a breakglass account managed by PAM itself. The user exist in PAM and the password for the login user is managed by PAM. If there is a breakglass process in place to extract and store password for breakglass accounts, the local breakglass store can also hold a local PAM user password for breakglass to PAM login access. The role of such a breakglass account is probably Global Administrator, but it can be anything necessary for emergency access to PAM.

Detailed setup and configuration in PAM is described in [PAM User for Breakglass](/docs/PAMUser-Breakglass.md).

## Manage local PAM user in different PAM environment

This scenario is using two independent PAM environments. The two environments are not part of the same PAM, but are really two different PAM servers or two independent PAM clusters, which are not connected to each other. The idea is to have a PAM administrator in one PAM environment being managed by the other PAM environment. If login to PAM GUI is automated, it is possible to be a standard user in the one PAM environment and login to the other PAM environment as administrator without knowing the password for the login user.

Detailed setup and configuration in PAM is described in [PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md).

## Automated login to PAM

Finally, a description about setup of PAM for automated login to the PAM GUI is avaialble. It can be used with managed local PAM users, and can also be used with Active Directory users. Regardless of the type of PAM user, automated login to PAM GUI is possible. 

Detailed setup and configuration in PAM is described in [Automated login to PAM GUI](/docs/PAMUser-Login.md).


# Build PAMUser connector

## Environment
The environment used is as follows:

- CentOS 9 (with SELINUX) for TCF server
- Java JDK, version 17.0.12
- Apache Tomcat, version 10.1.30
- Apache Maven, version 3.9.9
- Symantec PAM, version 4.2.3
- capamextensioncore, version 4.21.0.82

## Installation

- Download the project sources from GitHub.
- Add the `capamextensioncore.jar` from Symantec PAM as part of local Maven repository.
- Edit the files `pamuser_messages.properties` and `PAMUserMessageConstants.java`
and adjust the message numbers suitable for the PAM TCF setup, such that the message numbers do not conflict with other TCF connectors.
- Run the command `mvnw package` to compile the connector.
- Copy the target connector `pamuser.war` to the Tomcat `webapps_targetconnector` directory.
- It is recommended to enable logging for this connector by adding the following to the
Tomcat `logging.properties` file.

```
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
