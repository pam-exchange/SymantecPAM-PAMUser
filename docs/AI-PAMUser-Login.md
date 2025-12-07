# Automated Login to PAM

When PAM users are managed by PAM, a common requirement is to automate login to the PSM GUI using PAM-managed users. In essence, the PAM GUI is just another web application for which automated login can be configured.

In PAM, automated login for web applications is implemented using a `TCP/UDP service`.  
Starting with PAM **4.2.3**, it is possible to use the built-in Symantec PAM browser to perform automated login to PAM as a web application. Earlier releases did support automated login to PAM using the built-in browser.

This description covers two scenarios, both involving **two separate PAM environments**. In each scenario, a user is *managed* in one environment but used for automated login to the *other* environment.

1. **Local PAM user** managed in one PAM environment and used for automated login in another.  
   See `PAM User` connector details in [Remote PAM](/docs/PAMUser-RemotePAM).

2. **Active Directory user** managed in AD and used for automated login to the remote PAM environment.  
   Both PAM environments must have access to the same AD.

> [!NOTE]
> Automated login can use **either** local PAM users **or** Active Directory users, but PAM cannot support both login types simultaneously.

---

# PAM Setup — Common to Local and AD Users

Some PAM settings apply regardless of whether local PAM users or Active Directory users are used. After these common settings, the configuration differs depending on the login user type.

In both cases, the goal is to allow a standard user with limited privileges to log in to the remote PAM GUI **without knowing** the password of the login account.

## TCP/UDP Service for PAM

Automated login definitions for web applications are configured in a TCP/UDP service in PAM.  
On **PAM-01**, create a service `PAM` using:

- **Application protocol:** Web Portal  
- **Login method:** Symantec PAM HTML Web SSO  
- **Browser type:** Symantec PAM Browser  

![TCP service for PAM Web](/docs/images/Login-PAM-01-TCP-service.png)

## PAM Device

Create a device entry for the remote PAM environment (e.g., PAM-02) if one does not already exist.  
Assign the `PAM` service to this device.

![PAM Service on device](/docs/images/Login-PAM-01-Device-Service.png)

## Learning How to Log In to the PAM GUI

Create an access policy for an administrator on PAM-01 that grants access to the PAM-02 device using the new `PAM` service.

On the administrator’s access page, two entries for `PAM` appear:

- **PAM (Learn)** — used to identify username, password, and submit elements  
- **PAM** — used after configuration

![PAM service not configured](/docs/images/Login-PAM-01-Service-NotConfigured.png)

Select **PAM (Learn)** and identify the *Account Name*, *Password*, and *Submit Button* fields.

![Learn - Username](/docs/images/Login-PAM-01-Learn-1.png)

Save the configuration using the icon in the upper-right corner. The automated login is now ready for use.

![PAM service configured](/docs/images/Login-PAM-01-Service-Configured.png)

## Standard User Accessing the PAM GUI

On PAM-01, create a standard user called `John Doe`.  
No special permissions are needed.

---

# Automated Login Using a **Local PAM User**

## PAM-02: Global Setting (Local Authentication)

During learning, PAM identifies only HTML elements; it does not change authentication type.  
The default authentication type in the remote environment (PAM-02) must therefore match the intended login method.

If automated login will use **local PAM users**, the default authentication type **must be set to Local**.

If multiple AD domains are configured, note that only users from the **first domain** can be used for automated login.

![Authentication Type - Local](/docs/images/Login-PAM-02-GlobalSetting-Auth-Local.png)

## PAM-02: Local PAM User

Create a local PAM user in the PAM-02 environment and configure it to be managed from PAM-01 via the `PAM User` connector.  
See: [Remote PAM](/docs/PAMUser-RemotePAM)

## PAM-01: Policy Setup (Local PAM User)

Create a policy for:

- **User:** `john.doe`  
- **Device:** `PAM-02`  

In **Services**, add the `PAM` service and select `super-PAM-02` as the login (target) account.

![Policy service](/docs/images/Login-PAM-01-Policy-1-Local.png)  
![Policy service - Login user](/docs/images/Login-PAM-01-Policy-2-Local.png)

## Verification

Log in to PAM-01 as **john.doe** and select the **PAM** Web Portal entry.

![PAM Login](/docs/images/Login-PAM-01-Access-StandardUser.png)

A spinning padlock appears, and then the PAM-02 dashboard opens.  
`john.doe` does **not** see the login password.

![PAM Login](/docs/images/Login-PAM-01-Access-GlobalAdmin-Local.png)

## Security Considerations

- The login user is a Global Administrator on PAM-02.  
- The password for the login user (target account) cannot be retrieved from PAM-01.  
- The password of the local PAM user can still be reset.  
- Enable session recording on PAM-01 to record all administrator actions.  
- Enable syslog monitoring for password changes of PAM login users.  
- If an ApiKey is used for password changes, its password can be retrieved. Monitor for password or account updates.

---

# Automated Login Using an **Active Directory User**

When using AD users for PAM automated login, **no PAM User connector** is required.  
Both PAM environments must be connected to the same AD.

> [!CAUTION]
> Using AD accounts for automated login places administrative control with AD administrators.  
> If PAM loses connectivity to AD, login using AD users will fail.

In the example:

- AD group: `AdminLogins`  
- AD user: `superRemote` (member of `AdminLogins`)  
- `superRemote` is used for Global Administrator access to PAM-02.

## PAM-02: Global Setting (LDAP Authentication)

If automated login uses AD users, the default authentication type **must be set to LDAP**.

Again: only users from the **first AD domain** in the list can be used for automated login.

![Authentication Type - LDAP](/docs/images/Login-PAM-02-GlobalSetting-Auth-AD.png)

## PAM-02: Synchronize User Group from AD

Import the LDAP group `AdminLogins` into PAM-02.

![AdminLogin Group](/docs/images/Login-PAM-02-UserGroup-AdminLogins-1-AD.png)  
![AdminLogin Users](/docs/images/Login-PAM-02-UserGroup-AdminLogins-2-AD.png)

Assign Global Administrator privileges to this group.

## PAM-01: Target Account for the AD Login User

In PAM-01, create a target account for the AD user `superRemote`.  
A master AD account is used for password management.

![Target Account](/docs/images/Login-PAM-01-TargetAccount-superRemote-1-AD.png)  
![Target Account](/docs/images/Login-PAM-01-TargetAccount-superRemote-2-AD.png)

Synchronize with AD and rotate the password.

- PAM-01 manages the AD user’s password.  
- PAM-02 uses the same AD user for administrator login.

## PAM-01: Device Group for PAM-02

Policies can only use target accounts available on the device or its device group.  
Create a device group `PAM-02-Group` and add the `PAM-02` device.

Assign a credential source containing the AD target accounts (device: `Active Directory`).

![Device Group](/docs/images/Login-PAM-01-DeviceGroup-AD.png)

Add the `PAM` service to the device group.

## PAM-01: Policy Setup (AD User)

Create a policy for:

- **User:** `john.doe`  
- **Device group:** `PAM-02-Group`  

Add the `PAM` service and assign the target account `super-PAM-02` as login account.

![Policy service](/docs/images/Login-PAM-01-Policy-1-AD.png)  
![Policy service - Login user](/docs/images/Login-PAM-01-Policy-2-AD.png)

## Verification

Log in as **john.doe** on PAM-01 and open the **PAM** Web Portal.

![PAM Login](/docs/images/Login-PAM-01-Access-StandardUser.png)

The PAM-02 Dashboard loads automatically with Global Administrator privileges.

![PAM Login](/docs/images/Login-PAM-01-Access-GlobalAdmin-AD.png)

## Security Considerations

- The login user is a Global Administrator on PAM-02.  
- Password of the login user is a target account managed in PAM-01 and cannot be retrieved.  
- It is possible to setup a target application and account for the login user in AD and rotate the password. Password for the target account and login PAM user can now be retrieved.- Enable session recording for auditing.  
- AD administrators can reset passwords directly.  
- AD administrators can add users to the `AdminLogins` group.  
  Monitoring or blocking group membership changes is recommended.
