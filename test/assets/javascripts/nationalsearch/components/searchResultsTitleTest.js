import SearchResultsTitle from './searchResultsTitle'
import { expect } from 'chai'
import { shallow } from 'enzyme'

describe('SearchResultsTitle component', () => {
  context('no results have received yet', () => {
    it('no text rendered', () => {
      const title = shallow(<SearchResultsTitle total={0} pageNumber={1} pageSize={10} resultsReceived={false} />)
      expect(title.text()).to.equal('')
    })
  })

  context('results have been received for but none found', () => {
    let title

    beforeEach(() => {
      title = shallow(<SearchResultsTitle total={0} pageNumber={1} pageSize={10} resultsReceived />)
    })
    it('h2 rendered', () => {
      expect(title.find('h2')).to.have.length(1)
    })
    it('0 results found rendered', () => {
      expect(title.text()).to.equal('0 results found')
    })
  })

  context('results fit on one page', () => {
    context('results have been searched for with two found', () => {
      let title

      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={2} pageNumber={1} pageSize={10} resultsReceived />)
      })
      it('h2 rendered', () => {
        expect(title.find('h2')).to.have.length(1)
      })
      it('showing 2 results found rendered', () => {
        expect(title.text()).to.equal('2 results found')
      })
    })
  })

  context('only one result found', () => {
    context('results have been searched for with two found', () => {
      let title

      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={1} pageNumber={1} pageSize={10} resultsReceived />)
      })
      it('h2 rendered', () => {
        expect(title.find('h2')).to.have.length(1)
      })
      it('showing  1 result (no plural) found rendered', () => {
        expect(title.text()).to.equal('1 result found')
      })
    })
  })

  context('results spread over several pages', () => {
    let title

    context('on first page', () => {
      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={21} pageNumber={1} pageSize={10} resultsReceived />)
      })
      it('total shown with results range ( 1 to 10) for this page shown', () => {
        expect(title.text()).to.equal('21 results found, showing 1 to 10')
      })
    })

    context('on second page', () => {
      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={21} pageNumber={2} pageSize={10} resultsReceived />)
      })
      it('total shown with results range (11 to 20) for this page shown', () => {
        expect(title.text()).to.equal('21 results found, showing 11 to 20')
      })
    })

    context('on third page', () => {
      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={21} pageNumber={3} pageSize={10} resultsReceived />)
      })
      it('total shown with remainder results range (21 to 21) for this page shown', () => {
        expect(title.text()).to.equal('21 results found, showing 21 to 21')
      })
    })

    context('with huge number of results', () => {
      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={1234567} pageNumber={3} pageSize={10} resultsReceived />)
      })
      it('total shown with formatted total', () => {
        expect(title.text()).to.equal('1,234,567 results found, showing 21 to 30')
      })
    })

    context('with huge number of results on high page number', () => {
      beforeEach(() => {
        title = shallow(<SearchResultsTitle total={1234567} pageNumber={123456} pageSize={10} resultsReceived />)
      })
      it('total shown with formatted total', () => {
        expect(title.text()).to.equal('1,234,567 results found, showing 1,234,551 to 1,234,560')
      })
    })
  })
})
