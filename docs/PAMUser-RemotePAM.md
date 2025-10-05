# PAM User on Remote PAM

Consider two isolated PAM environments. Each of these can be clustered, but the two environments are not part of the same PAM cluster. The aim here is to use the `PAM User` connector for managing PAM login user in one PAM environment from a different PAM environment. Ideally you would use automated login to PAM GUI using a password not visible to a user. This is described elsewhere.

![PAM User for Breakglass](/docs/images/PAMUser-RemotePAM.png)

In the example here there are many moving parts with two different PAM environments and users and target accounts created in one environment being linked to target accounts in the other PAM environment. In the description below, the two environments are named `PAM-01` and `PAM-02` and the configuration in PAM is clearly marked to which of the two environments the configuration belongs.

- PAM-01 is the environment where accounts are managed from.
- PAM-02 is the environment where the local PAM user exist.

In the example used here PAM-01 is seeing PAM-02 as an end-point using the `PAM User`connector for managing accounts (local PAM users). The same approach can be used the other way around letting PAM-02 manage a local PAM user in PAM-01. This is not described here.


# PAM-02: PAM setup

The PAM environment PAM-02 is where the local PAM user exist. Seen from PAM-01 the environment PAM-02 is just another target environment where local accounts are defined and managed by PAM (PAM-01). 

## PAM-02: Global Settings

An important update on PAM-02 is the password length for local PAM users. Default length is 16 characters, which probably is insufficient. Edit the PAM system Global Settings and change the password length to 72 characters (maximum length).

![Password Length for PAM Users](/docs/images/GlobalSettings.png)


## PAM-02: PAM User

In the example used here, the local PAM user in PAM-02 has the role `Global Administrator`. It is possible to define other roles and permissions for the managed user. If so, the ApiKey associated with the local PAM user must have permissions to list and search target accounts and target servers. It is also possible to limit the scope of accounts for the ApiKey by using a target group. This is not used in the example shown here.

![PAM User - Basic Info](/docs/images/PAM-02-User-1.png)

The role for the PAM user in the example is a `Global Administrator`. 

![PAM User - Roles](/docs/images/PAM-02-User-2.png)

A `Global Administrator` must also have at least one Credential Manager Group associated. 

![PAM User - Credential Manager Groups](/docs/images/PAM-02-User-3.png)

Finally, define an ApiKey for the local PAM user. Initially when the ApiKey is created the ID is zero (0), and will be updated when the user is saved and the ApiKey is created.

![PAM User - ApiKey](/docs/images/PAM-02-User-4.png)


## PAM-02: ApiKey

The ApiKey is used when the `PAM User` connector is requesting a password update for a PAM user.

It is created as a standard ApiKey when the local PAM user is created. In the example here, the length the ApiKey is updated and the password age enforcement is turned off.


### PAM-02 - Password Composition Policy

The PCP used for the of ApiKey defined in PAM-02 will be used in PAM-01, and in PAM-02 this ApiKey must be **static**, i.e. there is no automatic password update of this ApiKey done by PAM-02 environment. Update to the ApiKey is done by PAM-01.

![PCP - ApiKey Static](/docs/images/PCP-ApiKey-static.png)


### PAM-02: ApiKey - TargetApplication

It is important to differentiate a standard ApiKey, which may have dynamic password update, with the static ApiKey used here. Create a new target application `ApiKey-static` and use the PCP without password age enforcement. The password length must be sufficiently high.

![TargetApplication - ApiKey Static](/docs/images/PAM-02-TargetApplication-ApiKey.png)

### PAM-02: ApiKey - TargetAccount

Finally, on PAM-02 locate the ApiKey created when the local PAM user was saved. 
Change the password to a new random value using the length and composition policy for the PCP defined for ApiKey-static. View the password for the ApiKey and make note of both username and password. They will be used on PAM-01. 

![TargetAccount - ApiKey](/docs/images/PAM-02-TargetAccount-ApiKey.png)

![TargetAccount - ApiKey Password](/docs/images/PAM-02-TargetAccount-ApiKey-Password.png)


# PAM-01: PAM setup

PAM-01 is where local PAM user and ApiKey on PAM-02 are managed from. From PAM-01 the other PAM environment is just an end-point using the `PAM User` connector.

## PAM-01: Password Composition Policy

Passwords for the target accounts created on PAM-01 for the local PAM user and ApiKey existing on PAM-02 must be changed automatically by PAM-01. For this purpose a new PCP is created using password age enforcement. Both the local PAM user and ApiKey (on PAM-02) will use passwords generated with this PCP, thus the length of the password must not be greater than the local PAM user passwords on PAM-02 (Global Settings). 

![PCP - PAM User Dynamic](/docs/images/PCP-PAMUser-Passwords-dynamic.png)

> [!NOTE]
> The compositon of passwords from this PCP must be aligned with PCP used on PAM-02 for ApiKeys and settings for local PAM user passwords. The PCP used on PAM-01 is dynamic and the PCP used on PAM-02 is static.


### PAM-01: TargetApplication

The same target application on PAM-01 is used for target accounts is used for both the ApiKey and local PAM user existing on PAM-02.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-1.png)

Use defaults for the tab `PAM User`, but consider that the communication path from the TCF server attached to PAM-01 to a PAM server in PAM-02.  

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-2.png)

In the tab `PAM User (remote)` verify that the checkbox `PAM is remote` is **checked**.

![TargetApplication - PAM User](/docs/images/PAM-01-TargetApplication-3.png)


### PAM-01: ApiKey - TargetAccount

The ApiKey used when PAM-01 sends API commands to PAM-02 using an ApiKey existing on PAM-02. The connector `PAM user` uses the accounht type setting `ApiKey` in the tab `PAM User`. The account name is the ApiKey account name generated when the local PAM user was created on PAM-02. The initial password for the target account for the ApiKey (on PAM-01) in the `PAM User` account is the password for the real ApiKey from PAM-02. 

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-1.png)

In the tab `Password` change the synchronize setting to update both PAM and end-point.

In the tab `PAM User` set the account type to **ApiKey**. In thte example used here there is no master account, thus leave it blank.

![TargetAccount - ApiKey](/docs/images/PAM-01-TargetAccount-ApiKey-2.png)


### PAM-01: PAM User - TargetAccount

Finally, create a target account using the account name identical to the local PAM user. Create new random value for the password. 

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-1.png)

In the tab `Password` change the synchronize setting to update both PAM and end-point.

In the tab `PAM User` Set the account type to `PAM User` and as master account use the target account for the ApiKey (on PAM-01). The master account is not a **real** ApiKey on PAM-01, but the target account of type `PAM User`.

![TargetAccount - PAM User](/docs/images/PAM-01-TargetAccount-User-2.png)

That's about it. Saving the target account will update the password for the PAM user to a new random value.


# Improvements and things to consider

- In the example here there is just one master account. It is possible to setup two ApiKey accounts for the local PAM user in PAM-02 and let PAM-01 manage both of them. In such a setup the two target accounts will be master accounts for each other. 

- Define target groups to limit scope of what an ApiKey can access.

- Define permissions for ApiKey to minimum required to update passwords on local PAM users.


# Error handling

If the API call to update the local PAM user on PAM-02 is failing, the API return code is visible in the `$CATALINA_HOME/logs/cataline.log` on the TCF server (associated with PAM-01). 

