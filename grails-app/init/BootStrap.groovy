import gr8data.Company
import gr8data.Country
import gr8data.GenderStats
import grails.converters.JSON
import groovy.json.JsonSlurper

@SuppressWarnings('DuplicateStringLiteral')
class BootStrap {

    private void addCountries() {
        if (!Country.count()) {
            new Country(name: 'United States of America', abbreviation: 'USA', continent: 'North America').save()
            new Country(name: 'United Kingdom', abbreviation: 'UK', continent: 'Europe').save()
            new Country(name: 'Australia', abbreviation: 'AU', continent: 'Australia').save()
            new Country(name: 'Spain', abbreviation: 'ES', continent: 'Europe').save()
            new Country(name: 'Belgium', abbreviation: 'BE', continent: 'Europe').save()
        }
    }

    private void readAndReformatStatsFromFile() {
        if (!Company.count()) {
            def jsonValues = new JsonSlurper().parseText('https://raw.githubusercontent.com/jlstrater/gr8ladies-d3/' +
                    'master/src/assets/data/data.json'.toURL().text)
            jsonValues.each {
                Country country = Country.findByAbbreviation(it.country) ?: Country.findByName(it.country)
                //TODO determine new country from name or pre-seed data
                if (!country) {
                    new Country(name: it.country, abbreviation: '//TODO', continent: '//TODO').save()
                }

                Company company = new Company(name: it.name, country: country, source: it.source).save()
                if (it.totalMen || it.totalWomen) {
                    GenderStats total = new GenderStats(name: 'Total', numberOfMen: it?.totalMen,
                            numberOfWomen: it?.totalWomen, company: company).save()
                    company.addToStats(total)
                }
                if (it.leadershipMen || it.leadershipWomen) {
                    GenderStats leadership = new GenderStats(name: 'Leadership', numberOfMen: it?.leadershipMen,
                            numberOfWomen: it?.leadershipWomen, company: company).save()
                    company.addToStats(leadership)
                }
                if (it.developersMen || it.developersWomen) {
                    GenderStats devs = new GenderStats(name: 'Developers', numberOfMen: it?.developersMen,
                            numberOfWomen: it?.developersWomen, company: company).save()
                    company.addToStats(devs)
                }
                if (it.qaMen || it.qaWomen) {
                    GenderStats qa = new GenderStats(name: 'QA', numberOfMen: it?.qaMen, numberOfWomen: it?.qaWomen,
                            company: company).save()
                    company.addToStats(qa)
                    company.save()
                }
            }
        }
    }

    def init = { servletContext ->

        addCountries()

        readAndReformatStatsFromFile()

        JSON.registerObjectMarshaller(Country) {
            [id: it.id, name: it.name, abbreviation: it.abbreviation, continent: it.continent]
        }

        JSON.registerObjectMarshaller(GenderStats) {
            [company: it.company.name, name: it.name, total: it.total, percentageWomen: it.percentageWomen,
                    numberOfWomen: it?.numberOfWomen, numberOfMen: it?.numberOfMen]
        }
    }
    def destroy = {
    }
}
