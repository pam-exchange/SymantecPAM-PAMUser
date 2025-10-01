# PAM User on Remote PAM

Consider two isolated PAM environments. Each of these can be clustered, but the two environments are not part of the same PAM cluster. The aim here is to use a `PAM User` connector to manage PAM login user in one PAM environment from a different PAM environment. Ideally you would use automated login to PAM GUI using a password not visible to a user, but that is not in scope for this example.

![PAM User for Breakglass](/docs/images/PAMUser-RemotePAM.png)

In the example here there are many moving parts with two different PAM environments and users and target accounts created in one environment being linked to target accounts in the other PAM environment. To help with understanding, the two environments are named `PAM-01` and `PAM-02` and the configuration in PAM is also clearly marked to which of the two environments the configuration belongs. 

- PAM-01 is the environment where accounts are managed from.
- PAM-02 is the environment where accounts exist.

In the example used here PAM-01 is seeing PAM-02 as an end-point where accounts exist. The same approach can be used by reversing the roles of PAM-01 and PAM-02.


# PAM-02: PAM setup

The PAM environment PAM-02 is where the local account (PAM user) exist. Seen from PAM-01 the environment PAM-02 is just another target environment where local accounts are defined and managed by PAM (PAM-01). 

## PAM-02: Global Settings

On PAM-02 where the PAM user is created, it is necessary to change the maximum password length for login users. This is done in the global settings. Update the Global Settings and change the password length to 72 characters (maximum length).

![Password Length for PAM Users](/docs/images/GlobalSettings.png)


## PAM-02: PAM User

The users being managed by PAM is in the example a Global Administrator. It is possible to define other roles and permissions for the user. If so, the ApiKey associated with the PAM user must have permissions to list and search target accounts and target servers. It is possible to define a target group with a limited view of target accounts. This is not used in the example here.

![PAM User - Basic Info](/docs/images/PAM-02-User-1.png)

The role for the PAM user in the example is a `Global Administrator`. 

![PAM User - Roles](/docs/images/PAM-02-User-2.png)

A `Global Administrator` must also have at least one Credential Manager Group associated. 

![PAM User - Credential Manager Groups](/docs/images/PAM-02-User-3.png)

Finally, for the user define an ApiKey. Initially when it is created the ID is zero (0). When the user is saved the ApiKey is created as a target account, and the ID will change to a different value

![PAM User - ApiKey](/docs/images/PAM-02-User-4.png)


## PAM-02: ApiKey

The ApiKey created when the user is created is a standard ApiKey. In the example here, the length and age of the ApiKey is updated, such that it is automatically updated when it reaches a defined age.

The ApiKey is used when the `PAM User` connector is requesting a password update for a PAM user.

### PAM-02 - Password Composition Policy

The PCP used for the of ApiKey used here must be **static**, i.e. there is no automatic update of the ApiKey done by PAM-02 environment. Update to the ApiKey is done by PAM-01.

![PCP - ApiKey Static](/docs/images/PCP-ApiKey-static.png)

> [!NOTE]
> The composition rules for the PCP used for ApiKeys on PAM-02 must match the composition rules for PAM User on PAM-01. Reason is that random passwords generated must be acceptable passwords for the ApiKey and PAM user on PAM-02. Password age enforcement is a difference between the two PCP used in PAM-01 and PAM-02. In PAM-02 the PCP is static. In PAM-01 the PCP is dynamic. 

### PAM-02: ApiKey - TargetApplication

It is important to differentiate a standard ApiKey with the dynamic ApiKey used here. A new target application `ApiKey-static` is created using the PCP with higher length and a age limit.

![TargetApplication - ApiKey Static](/docs/images/PAM-02-TargetApplication-ApiKey.png)

### PAM-02: ApiKey - TargetAccount

Finally, for the ApiKey the internal ID is added to the name used when the PAM user was created. Identify the correct target account and change the target application to `ApiKey-dynamic`.

![TargetAccount - ApiKey](/docs/images/PAM-02-TargetAccount-ApiKey.png)

When updating the target account for the ApiKey verify that it is synchronizing with PAM and that a new random password is generated with the length defined in the PCP.

The ApiKey account exist on PAM-02 and is managed by PAM-01. To setup the target account in PAM-01 it is necessary to view/copy the current password of the ApiKey and use it later when creating a target account in PAM-01.

![TargetAccount - ApiKey Password](/docs/images/PAM-02-TargetAccount-ApiKey-Password.png)


# PAM-01: PAM setup

This is the PAM environment from where the PAM user on PAM-02 is managed. Alas, for PAM-01 the PAM-02 environment is just a target system or end-point.

## PAM-01: Password Composition Policy

When creating new random passwords for PAM users a new PCP is used. The PCP uses a password age enforcement to let PAM-01 change the password for the PAM user on PAM-02 frequently. 

![PCP - PAM User Dynamic](/docs/images/PCP-PAMUser-Passwords-dynamic.png)

> [!NOTE]
> The compositon of passwords from this PCP must be aligned with PCP used on PAM-02. Also note that this PCP is used for random passwords for both the ApiKey and PAM user on PAM-02. The PCP used on PAM-01 is dynamic and the PCP used on PAM-02 is static.


### PAM-01: TargetApplication

The same target application is used for both the ApiKey and PAM user existing on PAM-02.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-1.png)

Use defaults for the tab `PAM User`. Consider that the communication is from the TCF server attached to PAM-01 to PAM server in PAM-02.  

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-2.png)


In the tab `PAM User (remote)` verify that the checkbox `PAM is remote` is **checked**.

![TargetApplication - PAM User](/docs/images/BG-TargetApplication-3.png)


### PAM-01: ApiKey - TargetAccount

The ApiKey used when PAM-01 sends API commands to PAM-02 exist on PAM-02. The connector `PAM user` is used with account type `ApiKey`. The password for the ApiKey (on PAM-02) is needed to setup the target account on PAM-01. The account name and account password is copied from the real ApiKey from PAM-02. 

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-1.png)

In the tab `Password` change the synchronize setting to update both PAM and end-point.

In the tab `PAM User` set the account type to **ApiKey**. There is no master account used in this example.

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-2.png)


### PAM-01: PAM User - TargetAccount

Finally, create a target account using the account name matching the PAM User name. Create new random value for the password. 

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-1.png)

In the tab `Password` change the synchronize setting to update both PAM and end-point.

In the tab `PAM User` Set the account type to `PAM User` and use the target account for the ApiKey just created. Not that this is not a **real** ApiKey on PAM-01.

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-2.png)

That's about it. Saving the target account will update the password for the PAM user to a new random value.


# Improvements and things to consider

- In the example here only a single master account is used. It is possible to setup two ApiKey accounts for the PAM user in PAM-02 and to manage both of them in PAM-01. In the setup in PAM-01 the two accounts are master for each other. 

- Create a TCP/UDP service in PAM-01, which will use the PAM user account managed to open a Web session to PAM-02 and perform automated login to PAM-02. This will allow a standard user in PAM-01 to become administrator in PAM-02 without knowing the password for the administrator in PAM-02.

# Error handling

If the API call to update the PAM user is failing, the API return code is visible in the `$CATALINA_HOME/logs/cataline.log` on the TCF server. 

