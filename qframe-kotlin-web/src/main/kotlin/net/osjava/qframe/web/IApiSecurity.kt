package net.osjava.qframe.web

interface IApiSecurity {
    fun validate(rc: RequestContext, apiSecurity: ApiSecurity): IResult<Any> {
        return ResultNothing()
    }
}