# Connector for PAM Users

The Symantec PAM connector here is used to manage password for PAM login users. Think of PAM as an end-point where the target account is a PAM login user. 

The `PAM User` connector is covering two scenarios. In addition to managing a local PAM user using the `PAM User` connector, there is also an example showing how to automate login to PAM using a local PAM user and an Active Directory user. 

Quick jump to detailed description

- [PAM User for Breakglass](/docs/PAMUser-Breakglass.md)
- [PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md)
- [Automated login to PAM GUI](/docs/PAMUser-Login.md)


## Breakglass account for PAM login

This scenario is having a local PAM user as a breakglass account managed by PAM itself. The user exist in PAM and the password for the login user is managed by PAM. If there is a process in place to extract and store password for breakglass accounts, the local breakglass store accounts can also hold a local PAM user for PAM login access. The role of such a breakglass account is probably Global Administrator, but it can be anything necessary for emergency access to PAM.

Detailed setup and configuration in PAM is described in [PAM User for Breakglass](/docs/PAMUser-Breakglass.md).

## Manage PAM login user in different PAM environment

This scenario is using two independent PAM environments. The environments are not setup in a PAM cluster, but are really two different PAM servers or two independent PAM clusters, which are not connected to each other. The idea is to have a PAM administrator in one PAM environment being managed by the other PAM environment. If login to PAM GUI is automated, it is possible to be a standard user in the one PAM environment and login to the other PAM environment as administrator without knowing the password for the login user.

Detailed setup and configuration in PAM is described in [PAM User on remote PAM](/docs/PAMUser-RemotePAM.md).

## Automated login to PAM

Finally, a description about setup of PAM for automated login to the PAM GUI is avaialble. It can be used with the scenario above using local PAM users. It is also shown how automated login to PAM GUI can be done using Active Directory users. 

Detailed setup and configuration in PAM is described in [Automated login to PAM](/docs/PAMUser-Login.md).


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
and adjust the message numbers to to match your environment.
It is important that the numbers does not conflict with any other numbers from other connectors.
- Run the command `mvnw package` to compile the connector.
- Copy the target connector `pamuser.war` to the Tomcat `webapps_targetconnector` directory.
- It is recommended to enable logging from the connector by adding the following to the
Tomcat `logging.properties` file.

```
#
# Target Connectors
#
ch.pam_exchange.pam_tc.pamuser.api.level = FINE
ch.pam_exchange.pam_tc.pamuser.api.handlers= java.util.logging.ConsoleHandler
```

# PAM setup and configuration

See the detailed documentation for PAM setup and configuration.


# Version history

1.0.0  
- Initial release
