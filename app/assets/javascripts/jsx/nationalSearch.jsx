
class ResultsRow extends React.Component {

    render() {
        return (
            <li>
                <span>{this.props.mistake}</span>
                <ul>
                    {this.props.suggestions.map(suggestion => (
                        <li>{suggestion}</li>
                    ))}
                </ul>
            </li>
        );
    }
}

class ResultsGrid extends React.Component {

    render() {
        return (
            <ul>
                {this.props.results.map(result => (
                    <ResultsRow {...result} />
                ))}
            </ul>
        );
    }
}

class OffenderSearch extends React.Component {

    constructor() {
        super();

        this.state = {
            name: "",
            results: []
        };
    }

    componentWillMount() {

        var webSocket = new ReconnectingWebSocket("ws://" + location.host + "/socket");

        webSocket.onmessage = event => this.setState({ results: JSON.parse(event.data) });

        this.performSearch = _.debounce(() => webSocket.send(JSON.stringify({ search: this.state.name })), this.props.delay);
    }

    render() {

        var searchChange = ev => this.setState({ name: ev.target.value }, this.performSearch);

        return (
            <div>
                <input value={this.state.name} onChange={searchChange} placeholder="Enter text here" />
                <ResultsGrid {...this.state} />
            </div>
        );
    }
}

ReactDOM.render(
    <OffenderSearch delay={500} />,
    document.getElementById('content')
);
