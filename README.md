# Connector for PAM Users

The Symantec PAM connector here is used to manage password for PAM login users. Think of PAM as an end-point where the target account is a PAM login user. 

The `PAM User` connector is covering two scenarios. In addition to managing a local PAM user using the `PAM User` connector, an example showing how to automate login to PAM without showing the password to the user. 

Quick jump to detailed description

- [PAM User for Breakglass](/docs/PAMUser-Breakglass.md)
- [PAM User on Remote PAM](/docs/PAMUser-RemotePAM.md)
- [Automated login to PAM GUI](/docs/PAMUser-Login.md)


## Breakglass account for PAM login

The scenario is to have a PAM login user as a breakglass account managed by PAM itself. The user exist in PAM and the password for the login user is managed by PAM. If there is a process in place to extract and store password for breakglass accounts, the local store for breakglass accounts will have a breakglass account for PAM login access. The role of such a breakglass account is probably Global Administrator, but it can be anything necessary for emergency access to PAM.

Detailed setup and configuration in PAM is described in [PAM User for Breakglass](/docs/PAMUser-Breakglass.md).

## Manage PAM login user in different PAM environment

The scenario is to have two independent PAM environments. The environments are not a PAM cluster, but really two different PAM servers or clusters, which are not connected to each other. The idea is to have a PAM administrator in a second PAM environment (PAM-02) being managed by the first PAM environment (PAM-01), and also in reverse where a PAM administrator in the first PAM environment (PAM-01) is being managed by the the other PAM environment (PAM-02).

Detailed setup and configuration in PAM is described in [PAM User on remote PAM](/docs/PAMUser-RemotePAM.md).

## Automated login to PAM

Furthermore, a setup and example is available using PAM to login to PAM. This is especially interesting when connecting from one PAM environment to a different PAM environment. A local PAM user managed in a PAM environment is used to login as administrator in a different PAM environment. 

Note that automated login to a PAM instance can use a local PAM user (as shown here), but keep in mind that other target account types can also be used. This is described in the detailed documentation. 

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

Setup and configuration in PAM is described in [PAM User for Breakglass](/docs/PAMUser-Breakglass.md) and [PAM User on remote PAM](/docs/PAMUser-RemotePAM.md),


# Version history

1.0.0  
- Initial release
