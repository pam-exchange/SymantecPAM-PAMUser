# Automated login to PAM

When PAM user login accounts are managed by PAM it is natural to consider automated login to PAM using the managed account. In essense PAM is just another web application, and automated login to this is doable.

In PAM the mechanism used for automated login to Web applications is a `TCP/UDP service`. With the newer releases of PAM (4.2.3++) it is possible to use the built-in Symantec PAM Web browser and to perform automated login to the PAM application. Using earlier releases of PAM the mechanism to use the built-in Web browser did not work correctly.

In the description here the PAM setup is covering two scenarios. Both scenarios are using two distinct and different PAM environments, where the PAM user is managed in one environment and used for automated login to the other PAM environment. 

1) Using a local PAM user existing in one PAM environment and managed by the other PAM environment. This is the setup using [Remote PAM](/docs/PAMUser-RemotePAM) for a local PAM user. 

2) Using Active Directory users for login to remote PAM environment (PAM-02) and managed as a target account in AD in first PAM environment (PAM-01). Both PAM environments must have access to the same Active Directory.

For reasons described later, it is not possible to mix the two scenarios for automated login to PAM. Users for automated login to PAM GUI must be either local PAM users or Active Directory users, but they cannot be mixed.


# PAM setup - Common for both Local and Active Directory PAM users

There are some PAM configuration used regardless of the type of PAM login user. Subsequently to the common setup the PAM setup and configuration will depend on the scenario when a local PAM user or an Active Directory user is used for login to PAM. 

In both scenarios the aim is to let a standard user login to PAM without knowing the login password to the PAM GUI.

## TCP/UDP service for PAM

Automated login definition for Web applications are defined in a TCP/UDP service in PAM. The definition is done on PAM-01. Create a new service `PAM` using application protocol **Web Portal**. The login method is **Symantec PAM HTML Web SSO**. The brower type is **Symantec PAM Browser**. 

![TCP service for PAM Web](/docs/images/Login-PAM-01-TCP-service.png)

## PAM device

Create or use the PAM device for the remote PAM environment (here PAM-02). Add the `PAM` service as an available service on the device.

![PAM Service on device](/docs/images/Login-PAM-01-Device-Service.png)

## Learning how to login to PAM GUI

On the PAM-01 access page the new Web service is available. It has two entries for the PAM service. One is used for learning where fields for username, password and submit button are located and one for using the PAM service. 

![PAM service not configured](/docs/images/Login-PAM-01-Service-NotConfigured.png)

Select the `PAM (Learn)` to start the configuration steps.

![Learn - Username](/docs/images/Login-PAM-01-Learn-1.png)

When the Accountname, Password and Submit Button fields are identified and marked, save the web page configuration (top right icon) and the PAM service is now ready to be used.

![PAM service configured](/docs/images/Login-PAM-01-Service-Configured.png)

## Standard user using automated login

On the calling PAM environment (PAM-01) create a new standard user `John Doe`. No special permissions is required for this user.


# Automated login using **local PAM User**

## PAM-02: Global setting (Local authentication)

When learning PAM about the PAM GUI above, the username field, password field and submit button are identified. There is no ability to change the authentication type. Whatever the defaule setting is, this is what is used. Verify the default authentication type in global setting on remote PAM environment (PAM-02). If Active Directory users are used for automated PAM login, the default authentication type **must** be set to `Local`. If the PAM environment uses multiple AD domains, only users from the first domain in the list can be used for automated login.

![Authentication Type - Local](/docs/images/Login-PAM-02-GlobalSetting-Auth-Local.png)

## PAM-02: Local PAM user

Setup a local PAM user in PAM-02 environment and use `PAM User` connector to managed it from PAM-01 environment. See [Remote PAM](/docs/PAMUser-RemotePAM) for details.

## PAM-01: Policy setup (local PAM user)

Create a new policy for a single user `john.doe` and a single device `PAM-02`.
In the tab `Services` add the `PAM` service and assign the target account `super-PAM-02` as login credentials.

![Policy service](/docs/images/Login-PAM-01-Policy-1-Local.png)
![Policy service - Login user](/docs/images/Login-PAM-01-Policy-2-Local.png)

## Verify automated login using local PAM user

Login to PAM-01 as user `john.doe` and select the Web Portal `PAM`. 

![PAM Login](/docs/images/Login-PAM-01-Access-StandardUser.png)

A spinning pad lock is shown. Eventually, the PAM Dashboard is shown for PAM-02. The user is a global administrator and the password for login is not revealed to user John Doe.

![PAM Login](/docs/images/Login-PAM-01-Access-GlobalAdmin-Local.png)


# Automated login using **Active Directory User**

When using PAM users from Active Directory the `PAM User` connector or any other connectors are **not** required for automated login to a remote PAM environment. It can be configured Out-Of-The-Bbox on the two PAM environments.

> [!CAUTION]
> Using an Active Directory account for automated login, control of who can login to PAM with high privileged (possible Global Administrator) is in the hands of the people controlling Active Directory.  
> In PAM it is an AD group membership which will determine if a user can login to PAM and the role and privileges are assigned to the group and indirectly to the user.  
> Furthermore, if connection from PAM to AD is failing, login as a PAM user from AD is not possible. 


In the example here, the AD group `AdminLogins` is created in Active Directory. An AD user `superRemote` is member of the group. This is the user used for login to PAM as global administrator. 

## PAM-02: Global setting (LDAP authentication)

When learning PAM about the PAM GUI above, the username field, password field and submit button are identified. There is no ability to change the authentication type. Whatever the defaule setting is, this is what is used. Verify the default authentication type in global setting on remote PAM environment (PAM-02). If Active Directory users are used for automated PAM login, the default authentication type **must** be set to `LDAP`. If the PAM environment uses multiple AD domains, only users from the first domain in the list can be used for automated login.

![Authentication Type - LDAP](/docs/images/Login-PAM-02-GlobalSetting-Auth-AD.png)

## PAM-02: Synchronize user group from AD

In PAM import an LDAP group from AD. Find the group **AdminLogins** and register it in PAM-02.

![AdminLogin Group](/docs/images/Login-PAM-02-UserGroup-AdminLogins-1-AD.png)
![AdminLogin Users](/docs/images/Login-PAM-02-UserGroup-AdminLogins-2-AD.png)

Configure the user group with the Global Administrator role and other settings necessary for a Global Administrator.

## PAM-01: TargetAccount for PAM login user

In the PAM-01 environment create a target account for the AD user `superRemote`. The setup here uses an AD master account to change password for other AD users. A detailed description for PAM setup is available in the Symantec PAM documentation. 

![Device Group](/docs/images/Login-PAM-01-TargetAccount-superRemote-1-AD.png)
![Device Group](/docs/images/Login-PAM-01-TargetAccount-superRemote-2-AD.png)

Synchronize the account with PAM and AD. Change the password to a new random value. 

PAM-01 uses the AD to manage the password on a user. PAM-02 uses the same AD user for login as global administrator. 

## PAM-01: Device group for PAM-02 device

When defining a policy for a device only target accounts defined on the device are available for automated login. Create a device group `PAM-02-Group` and add the `PAM-02` device as a member. On the device group specify a credential source where login users (target accounts) for automated login exist. The device for active directory used in the example is called `Active Directory`. 

![Device Group](/docs/images/Login-PAM-01-DeviceGroup-AD.png)

Add the service `PAM` to the device group. The `PAM` service is also assigned to the PAM-02 device.

## PAM-01: Policy setup (AD user)

Create a new policy for a single user `john.doe` and a **device group** `PAM-02-Group`. 
In the tab `Services` add the `PAM` service and assign the target account `super-PAM-02` as login credentials.

![Policy service](/docs/images/Login-PAM-01-Policy-1-AD.png)
![Policy service - Login user](/docs/images/Login-PAM-01-Policy-2-AD.png)

## Verify automated login using AD user

Login to PAM-01 as user `john.doe` and select the Web Portal `PAM`. 

![PAM Login](/docs/images/Login-PAM-01-Access-StandardUser.png)

A spinning pad lock is shown. Eventually, the PAM Dashboard is shown for PAM-02. The user is a global administrator and the password for login is not revealed to user John Doe.

![PAM Login](/docs/images/Login-PAM-01-Access-GlobalAdmin-AD.png)

