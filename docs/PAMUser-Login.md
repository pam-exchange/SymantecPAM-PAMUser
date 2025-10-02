# Automated login to PAM

This description and setup is about automated login to PAM for a user without he/she knows the login password.

The setup is as follows:  
A standard user without administration privilegeds in PAM-01 environment is using the local PAM user in PAM-02 to become Global Administrator in PAM-02.

Seen from PAM-01 all PAM servers in PAM-02 environment are just another Web Service and the configuraiton is done using a TCP service, and define the login process for the web server, and finally assign a login user in the access policy. The following is a description of the configuration.

# PAM setup

## TCP/UDP service for PAM

This service is a Web service login for PAM. It is defined on PAM-01. 

![TCP service for PAM Web](/docs/images/Login-TCP-service.png)

The login method is using HTML Web SSO, and the built-in `Symantec PAM Browser`. 
Note that this is only working with PAM 4.2.3 and newer. 

## Learn how to login

As an administrator in PAM-01 add the new `PAM` service to the PAM-02 device as a service. Switch to the `Access page` and observe that the service is avaialble, but not configured.

![PAM service not configured](/docs/images/Login-Service-NotConfigured.png)

Select the `PAM (Learn)` to start the configuration steps.

![Learn - Username](/docs/images/Login-Learn-1.png)
![Learn - Password](/docs/images/Login-Learn-2.png)
![Learn - Submit](/docs/images/Login-Learn-3.png)

In the top right corner select the disk icon to save the configuration.
The Access page changes and the PAM service is now ready to be used.

![PAM service configured](/docs/images/Login-Service-Configured.png)

## Policy setup

A policy for a standard user `John Doe` is created and the `PAM Service` is used for device `PAM-02`. In the policy the target account used for automated login is the account `super-PAM-02`, which is a local PAM user in the PAM-02 environment.

![Policy service](/docs/images/Login-Policy-Service.png)

# Login to PAM

Login to PAM-01 as user `john.doe`. On the users Access page the Web service `PAM` is available and ready to use.

![Standard User in PAM-01](/docs/images/Login-Access-StandardUser.png)

Select the **PAM** Web service, observe a spinning padlock and login to PAM-02 as Global Administrator is done using the local PAM user `super-PAM-02`.

![Global Administator in PAM-02](/docs/images/Login-Access-GlobalAdmin.png)

The login is done without showing the login password. The user is now Global Administrator in PAM-02, but the password for the local user `super-PAM-02` is not found in PAM-02, thus there is no direct access to the password from PAM-02.

It is possible to fetch the password for the ApiKey used to change passwords for the local PAM user. With the ApiKey it is possible to use the API to set the password for the local PAM user `supr-PAM-02` or any other user to a known value.

# Alternative to PAM local user

It is possible to use an alternative mechanism than local PAM users and the `PAM User` connector. If the two PAM environments share a common Active Directory or LDAP server, it is possible to setup a login user in PAM-02 for a user in AD/LDAP. In PAM-01 you can create a target account for the same AD/LDAP user and change the password. In PAM-01 setup the policy using the AD/LDAP target account as login user to the PAM service. 

![Login user is AD/LDAP](/docs/images/Login-AD.png)

In PAM-02 the login user is a AD/LDAP user. The system has no knowledge of the password being managed in PAM-01 and will happily accept login with this AD/LDAP user.

An advantage is that there is no ApiKey required for password update of a PAM user (in PAM-02). 
A disadvantage is that the two PAM environments both must have access to the same AD/LDAP.

