package com.app.iuafc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The five user roles, same as the website (`lib/types.ts`). */
@Serializable
enum class Role(val displayName: String) {
    @SerialName("Administrator") ADMINISTRATOR("Administrator"),
    @SerialName("Pastor") PASTOR("Pastor"),
    @SerialName("Finance Officer") FINANCE_OFFICER("Finance Officer"),
    @SerialName("Ministry Leader") MINISTRY_LEADER("Ministry Leader"),
    @SerialName("Church Member") CHURCH_MEMBER("Church Member");
}
