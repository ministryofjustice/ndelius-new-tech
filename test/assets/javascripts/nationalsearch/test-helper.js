const offender = (offender = {}) =>
    Object.assign({
        offenderId: '123',
        firstName: 'John',
        surname: 'Smith',
        dateOfBirth: '19-07-1965',
        otherIds: {
            crn: 'D123X'
        },
        currentDisposal: "1",
        offenderProfile: {
            riskColour: 'red'
        },
        gender: 'Male',
        aliases: [],
        addresses: [],
        age: 19,
    }, offender)


export {offender};