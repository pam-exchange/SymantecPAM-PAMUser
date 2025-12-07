# PAM User for Breakglass

The `PAM User` connector is used to manage passwords for local PAM users.  
The example here describes how to create a local PAM user and let PAM rotate the password automatically.  
A common use case is creating a breakglass user for the PAM system itself.

![PAM User for Breakglass](/docs/images/PAMUser-Breakglass.png)

More information about breakglass scenarios and how Symantec PAM can store breakglass passwords outside PAM is available in the repo **Breakglass**:  
https://github.com/pam-exchange/Breakglass

Password updates for local PAM users are done using the **API**, not the CLI.  
Only the API allows disabling the “change password at next login” requirement.  
The CLI always forces a password change on next login.

Using the API requires an **ApiKey** linked to the local PAM user.

> [!NOTE]  
> In this example a **new** local PAM user is created.  
> The same approach can be used for the built-in administrator `super`.

---

# PAM Setup

## Global Settings

Increase the default password length for local PAM users.  
The default is 16 characters, which is usually too short.  
Update the global setting to the maximum value: **72 characters**.

![Password Length for PAM Users](/docs/images/GlobalSettings-User-Password-Length.png)

## PAM User (Login)

The local PAM user managed in this example has the `Global Administrator` role.  
Other roles can be used, but the user’s ApiKey must have permissions to list/search target accounts and servers.  
Target groups may also be used to restrict the scope, though this example does not require them.

![PAM User - Basic Info](/docs/images/BG-User-1.png)

The role assigned is `Global Administrator`.

![PAM User - Roles](/docs/images/BG-User-2.png)

A `Global Administrator` must also have a Credential Management Group assigned.

![PAM User - Credential Manager Groups](/docs/images/BG-User-3.png)

Finally, create an ApiKey for the user.  
When first created, the GUI shows the ID as **0**.  
After saving, PAM assigns the real ApiKey ID.

![PAM User - ApiKey](/docs/images/BG-User-4.png)

## ApiKey

When creating a new local PAM user, this example also creates a dedicated **dynamic ApiKey**.  
This ApiKey is used by the `PAM User` connector to update the user’s password.

### ApiKey – Password Composition Policy

The ApiKey uses a dynamic PCP with a password age limit of **1 day**.  
You may choose a different schedule.

![PCP - ApiKey Dynamic](/docs/images/PCP-ApiKey-dynamic.png)

### ApiKey – Target Application

Create a new target application `ApiKey-dynamic` that uses the stronger PCP.

![TargetApplication - ApiKey Dynamic](/docs/images/BG-TargetApplication-ApiKey.png)

### ApiKey – Target Account

Locate the target account created for the ApiKey.  
Update the target application to `ApiKey-dynamic`.

![TargetAccount - ApiKey](/docs/images/BG-TargetAccount-ApiKey.png)

After updating, verify that PAM is synchronizing passwords and generate a new random password.

## PAM User (Managed)

Create a target application and target account using the **same username** as the local PAM user.

## PAM User – Password Composition Policy

The PCP for PAM user passwords must not exceed the PAM global maximum.  
Create a PCP to generate new passwords for the `PAM User` connector.

For breakglass accounts, the PCP should **not** enforce automatic password aging.

![PCP - PAM User Static](/docs/images/PCP-PAMUser-Passwords-static.png)

### PAM User – Target Application

The PAM User connector updates an account on a target server, which is the PAM appliance itself.  
In a cluster, use a node from the **primary site**.

Create the target application using the PCP made for PAM User passwords.

![TargetApplication - PAM User](/docs/images/BG-TargetApplication-User-1.png)

Use default settings on the '**PAM User**' tab.

![TargetApplication - PAM User](/docs/images/BG-TargetApplication-User-2.png)

On the '**PAM User (remote)**' tab, ensure the setting '**PAM is remote**' is **unchecked**.

![TargetApplication - PAM User](/docs/images/BG-TargetApplication-User-3.png)

### PAM User – Target Account

Create the target account with the same username as the local PAM user.  
Generate a new random password.

![TargetAccount - PAM User](/docs/images/BG-TargetAccount-User-1.png)

On the '**Password**' tab, enable synchronization for both PAM and the endpoint.

On the '**PAM User**' tab, set the account type to `PAM User` and select the ApiKey created for the user as the master account.

![TargetAccount - PAM User](/docs/images/BG-TargetAccount-User-2.png)

After saving, the `PAM User` connector updates the local PAM user’s password.

---

# Improvements and Considerations

- You may configure two ApiKey accounts for the local PAM user and let PAM rotate both.  
  Each ApiKey becomes the master for the other.
- Use target groups to limit ApiKey visibility.
- Assign only the minimum required ApiKey permissions.

---

# Error Handling

If the API call fails, check the return code in:  
`$CATALINA_HOME/logs/catalina.log` on the TCF server.
